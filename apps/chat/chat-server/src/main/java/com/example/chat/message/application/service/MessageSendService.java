package com.example.chat.message.application.service;

import com.example.chat.auth.core.util.SecurityUtils;
import com.example.chat.common.core.exception.ChatErrorCode;
import com.example.chat.exception.ChatException;
import com.example.chat.message.application.dto.request.SendMessageRequest;
import com.example.chat.message.application.dto.response.MessageResponse;
import com.example.chat.message.domain.MessageContent;
import com.example.chat.message.infrastructure.messaging.MessageEventPublisher;
import com.example.chat.storage.entity.ChatChannelEntity;
import com.example.chat.storage.entity.ChatMessageEntity;
import com.example.chat.storage.entity.UserEntity;
import com.example.chat.storage.repository.JpaChannelMemberRepository;
import com.example.chat.storage.repository.JpaChannelRepository;
import com.example.chat.storage.repository.JpaMessageRepository;
import com.example.chat.storage.repository.JpaUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class MessageSendService {

    private static final Logger log = LoggerFactory.getLogger(MessageSendService.class);

    private final JpaMessageRepository messageRepository;
    private final JpaChannelRepository channelRepository;
    private final JpaChannelMemberRepository channelMemberRepository;
    private final JpaUserRepository userRepository;
    private final MessageEventPublisher messageEventPublisher;

    public MessageSendService(
            JpaMessageRepository messageRepository,
            JpaChannelRepository channelRepository,
            JpaChannelMemberRepository channelMemberRepository,
            JpaUserRepository userRepository,
            MessageEventPublisher messageEventPublisher) {
        this.messageRepository = messageRepository;
        this.channelRepository = channelRepository;
        this.channelMemberRepository = channelMemberRepository;
        this.userRepository = userRepository;
        this.messageEventPublisher = messageEventPublisher;
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

        ChatMessageEntity message = buildMessageEntity(request, senderId);
        ChatMessageEntity saved = messageRepository.save(message);
        saved.markAsSent();
        messageRepository.save(saved);

        publishEvent(saved);

        log.info("Message sent: messageId={}, channelId={}, senderId={}",
                saved.getId(), channel.getId(), senderId);
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

    private void publishEvent(ChatMessageEntity saved) {
        try {
            messageEventPublisher.publishMessageSent(saved);
        } catch (Exception e) {
            log.error("Failed to publish message event: messageId={}", saved.getId(), e);
        }
    }
}
