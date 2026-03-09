package com.example.chat.message.application.service;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.chat.auth.core.util.SecurityUtils;
import com.example.chat.common.core.exception.ChatErrorCode;
import com.example.chat.exception.ChatException;
import com.example.chat.message.application.dto.request.SendMessageRequest;
import com.example.chat.message.application.dto.response.MessageResponse;
import com.example.chat.message.domain.MessageContent;
import com.example.chat.message.infrastructure.kafka.KafkaMessageProducer;
import com.example.chat.message.infrastructure.messaging.MessageEventPublisher;
import com.example.chat.storage.entity.ChatChannelEntity;
import com.example.chat.storage.entity.ChatMessageEntity;
import com.example.chat.storage.entity.UserEntity;
import com.example.chat.storage.repository.JpaChannelMemberRepository;
import com.example.chat.storage.repository.JpaChannelMetadataRepository;
import com.example.chat.storage.repository.JpaChannelRepository;
import com.example.chat.storage.repository.JpaMessageRepository;
import com.example.chat.storage.repository.JpaUserRepository;

@Service
@Transactional(readOnly = true)
public class MessageSendService {

    private static final Logger log = LoggerFactory.getLogger(MessageSendService.class);

    private final JpaMessageRepository messageRepository;
    private final JpaChannelRepository channelRepository;
    private final JpaChannelMemberRepository channelMemberRepository;
    private final JpaChannelMetadataRepository channelMetadataRepository;
    private final JpaUserRepository userRepository;
    private final MessageEventPublisher messageEventPublisher;
    private final KafkaMessageProducer kafkaMessageProducer;

    public MessageSendService(
            JpaMessageRepository messageRepository,
            JpaChannelRepository channelRepository,
            JpaChannelMemberRepository channelMemberRepository,
            JpaChannelMetadataRepository channelMetadataRepository,
            JpaUserRepository userRepository,
            MessageEventPublisher messageEventPublisher,
            KafkaMessageProducer kafkaMessageProducer) {
        this.messageRepository = messageRepository;
        this.channelRepository = channelRepository;
        this.channelMemberRepository = channelMemberRepository;
        this.channelMetadataRepository = channelMetadataRepository;
        this.userRepository = userRepository;
        this.messageEventPublisher = messageEventPublisher;
        this.kafkaMessageProducer = kafkaMessageProducer;
    }

    @Transactional
    public MessageResponse sendMessage(SendMessageRequest request) {
        log.info("Sending message: channelId={}, type={}", request.channelId(), request.messageType());

        String senderId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));

        ChatChannelEntity channel = channelRepository.findById(request.channelId())
                .orElseThrow(() -> new ChatException(ChatErrorCode.CHANNEL_NOT_FOUND));

        UserEntity sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.USER_NOT_FOUND));

        validateSendPermission(channel, sender, request.channelId());

        // 채널 멤버 수 조회 (unreadCount 초기값 설정용)
        long memberCount = channelMemberRepository.countByChannelId(request.channelId());

        ChatMessageEntity message = buildMessageEntity(request, senderId);
        ChatMessageEntity saved = messageRepository.save(message);
        saved.markAsSent();
        saved.initUnreadCount((int) memberCount);
        messageRepository.save(saved);

        // 발신자 제외 모든 멤버의 unreadCount 일괄 증가 (단일 UPDATE)
        int updatedRows = channelMetadataRepository.bulkIncrementUnreadCount(request.channelId(), senderId);
        // 발신자 lastActivityAt 갱신
        channelMetadataRepository.updateLastActivity(request.channelId(), senderId);
        log.debug("Incremented unreadCount for {} members in channel={}", updatedRows, request.channelId());

        // Redis Pub/Sub으로 실시간 브로드캐스트 (unreadCount 포함)
        publishEvent(saved, (int) memberCount);

        // Kafka 푸시 알림 (오프라인 사용자 포함, 발신자 제외)
        sendPushNotifications(request.channelId(), senderId, saved);

        log.info("Message sent: messageId={}, channelId={}, senderId={}, memberCount={}",
                saved.getId(), channel.getId(), senderId, memberCount);
        return MessageResponse.fromEntity(saved);
    }

    private void validateSendPermission(ChatChannelEntity channel, UserEntity sender, String channelId) {
        if (!channel.isActive()) throw new ChatException(ChatErrorCode.CHANNEL_NOT_ACTIVE);
        if (!channelMemberRepository.existsByChannelIdAndUserId(channelId, sender.getId()))
            throw new ChatException(ChatErrorCode.CHANNEL_NOT_MEMBER);
        if (!sender.isActive()) throw new ChatException(ChatErrorCode.USER_NOT_ACTIVE);
    }

    private ChatMessageEntity buildMessageEntity(SendMessageRequest request, String senderId) {
        var content = request.toMessageContent();
        var builder = ChatMessageEntity.builder()
                .id(UUID.randomUUID().toString())
                .channelId(request.channelId())
                .senderId(senderId)
                .messageType(request.messageType());

        switch (content) {
            case MessageContent.Text t ->
                    builder.contentText(t.text());
            case MessageContent.Image i ->
                    builder.contentMediaUrl(i.mediaUrl())
                           .contentFileName(i.fileName())
                           .contentFileSize(i.fileSize());
            case MessageContent.File f ->
                    builder.contentMediaUrl(f.mediaUrl())
                           .contentFileName(f.fileName())
                           .contentFileSize(f.fileSize())
                           .contentMimeType(f.mimeType());
        }
        return builder.build();
    }

    private void publishEvent(ChatMessageEntity saved, int memberCount) {
        try {
            messageEventPublisher.publishMessageSent(saved, memberCount);
        } catch (Exception e) {
            log.error("Failed to publish message event: messageId={}", saved.getId(), e);
        }
    }

    /**
     * 발신자를 제외한 모든 채널 멤버에게 Kafka 푸시 알림 발행
     */
    private void sendPushNotifications(String channelId, String senderId, ChatMessageEntity saved) {
        try {
            String pushContent = switch (saved.getMessageType()) {
                case TEXT, SYSTEM -> saved.getContentText() != null ? saved.getContentText() : "";
                case IMAGE        -> "[이미지]";
                case FILE, VIDEO, AUDIO -> "[파일] " + saved.getContentFileName();
            };
            List<String> receiverIds = channelMemberRepository.findByChannelId(channelId)
                    .stream()
                    .map(m -> m.getUserId())
                    .filter(uid -> !uid.equals(senderId))
                    .toList();

            for (String receiverId : receiverIds) {
                kafkaMessageProducer.publishNotification(receiverId, "새 메시지", pushContent, "CHAT_MESSAGE");
            }
            log.debug("Push notifications sent: channelId={}, receivers={}", channelId, receiverIds.size());
        } catch (Exception e) {
            log.error("Failed to send push notifications: channelId={}", channelId, e);
        }
    }
}
