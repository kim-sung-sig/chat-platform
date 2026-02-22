package com.example.chat.message.application.service;

import com.example.chat.auth.core.util.SecurityUtils;
import com.example.chat.domain.channel.Channel;
import com.example.chat.domain.channel.ChannelId;
import com.example.chat.domain.channel.ChannelRepository;
import com.example.chat.domain.message.Message;
import com.example.chat.domain.message.MessageContent;
import com.example.chat.domain.message.MessageRepository;
import com.example.chat.domain.message.MessageType;
import com.example.chat.domain.service.MessageDomainService;
import com.example.chat.domain.user.User;
import com.example.chat.domain.user.UserId;
import com.example.chat.domain.user.UserRepository;
import com.example.chat.message.application.dto.request.SendMessageRequest;
import com.example.chat.message.application.dto.response.MessageResponse;
import com.example.chat.message.infrastructure.messaging.MessageEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * 메시지 애플리케이션 서비스 (Use Case)
 */
@Service
@Transactional(readOnly = true)
public class MessageApplicationService {
    private static final Logger log = LoggerFactory.getLogger(MessageApplicationService.class);

    private final MessageRepository messageRepository;
    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;
    private final MessageDomainService messageDomainService;
    private final MessageEventPublisher messageEventPublisher;

    public MessageApplicationService(
            MessageRepository messageRepository,
            ChannelRepository channelRepository,
            UserRepository userRepository,
            MessageDomainService messageDomainService,
            MessageEventPublisher messageEventPublisher) {
        this.messageRepository = messageRepository;
        this.channelRepository = channelRepository;
        this.userRepository = userRepository;
        this.messageDomainService = messageDomainService;
        this.messageEventPublisher = messageEventPublisher;
    }

    /**
     * 메시지 발송 Use Case
     */
    @Transactional
    public MessageResponse sendMessage(SendMessageRequest request) {
        log.info("Sending message: channelId={}, type={}", request.channelId(), request.messageType());

        // Step 1: 인증 확인
        UserId senderId = getUserIdFromContext();

        // Step 2: 필수 파라미터 검증
        if (request.channelId() == null || request.channelId().isBlank()) {
            throw new IllegalArgumentException("Channel ID is required");
        }

        // Step 3: Aggregate 조회 - Channel
        Channel channel = findChannelById(request.channelId());

        // Step 4: Aggregate 조회 - User
        User sender = findUserById(senderId);

        // Step 5: Domain Service 호출 - 메시지 생성
        Message message = createMessageByType(channel, sender, request);

        // Step 6: 저장
        Message savedMessage = messageRepository.save(message);

        // Step 7: 이벤트 발행
        publishMessageEvent(savedMessage);

        log.info("Message sent successfully: messageId={}, channelId={}, senderId={}",
                savedMessage.getId().value(), channel.getId().value(), sender.getId().value());

        // Step 8: Response 변환
        return convertToResponse(savedMessage);
    }

    // ========== Private Helper Methods ==========

    private UserId getUserIdFromContext() {
        return SecurityUtils.getCurrentUserId()
                .map(UserId::of)
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));
    }

    private Channel findChannelById(String channelIdStr) {
        ChannelId channelId = ChannelId.of(channelIdStr);
        return channelRepository.findById(channelId)
                .orElseThrow(() -> new IllegalArgumentException("Channel not found: " + channelIdStr));
    }

    private User findUserById(UserId userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId.value()));
    }

    private Message createMessageByType(Channel channel, User sender, SendMessageRequest request) {
        MessageType type = request.messageType();
        if (type == null)
            throw new IllegalArgumentException("Message type is required");

        return switch (type) {
            case TEXT -> {
                String text = extractTextField(request, "text");
                yield messageDomainService.createTextMessage(channel, sender, text);
            }
            case IMAGE -> {
                String imageUrl = extractTextField(request, "imageUrl");
                String imageName = extractTextFieldOrDefault(request, "fileName", "image.jpg");
                long imageSize = extractLongFieldOrDefault(request, "fileSize", 0L);
                yield messageDomainService.createImageMessage(channel, sender, imageUrl, imageName, imageSize);
            }
            case FILE -> {
                String fileUrl = extractTextField(request, "fileUrl");
                String fileName = extractTextField(request, "fileName");
                long fileSize = extractLongFieldOrDefault(request, "fileSize", 0L);
                String mimeType = extractTextFieldOrDefault(request, "mimeType", "application/octet-stream");
                yield messageDomainService.createFileMessage(channel, sender, fileUrl, fileName, fileSize, mimeType);
            }
            case VIDEO -> {
                String videoUrl = extractTextField(request, "videoUrl");
                String fileName = extractTextFieldOrDefault(request, "fileName", "video.mp4");
                long fileSize = extractLongFieldOrDefault(request, "fileSize", 0L);
                String mimeType = extractTextFieldOrDefault(request, "mimeType", "video/mp4");
                yield messageDomainService.createFileMessage(channel, sender, videoUrl, fileName, fileSize, mimeType);
            }
            case AUDIO -> {
                String audioUrl = extractTextField(request, "audioUrl");
                String fileName = extractTextFieldOrDefault(request, "fileName", "audio.mp3");
                long fileSize = extractLongFieldOrDefault(request, "fileSize", 0L);
                String mimeType = extractTextFieldOrDefault(request, "mimeType", "audio/mpeg");
                yield messageDomainService.createFileMessage(channel, sender, audioUrl, fileName, fileSize, mimeType);
            }
            case SYSTEM -> {
                String systemText = extractTextField(request, "text");
                yield messageDomainService.createSystemMessage(channel, systemText);
            }
        };
    }

    private String extractTextField(SendMessageRequest request, String fieldName) {
        Map<String, Object> payload = request.payload();
        if (payload == null)
            throw new IllegalArgumentException("Payload is required");
        Object value = payload.get(fieldName);
        if (value == null)
            throw new IllegalArgumentException("Field '" + fieldName + "' is required in payload");
        return value.toString();
    }

    private String extractTextFieldOrDefault(SendMessageRequest request, String fieldName, String defaultValue) {
        Map<String, Object> payload = request.payload();
        if (payload == null)
            return defaultValue;
        Object value = payload.get(fieldName);
        return value != null ? value.toString() : defaultValue;
    }

    private long extractLongFieldOrDefault(SendMessageRequest request, String fieldName, long defaultValue) {
        Map<String, Object> payload = request.payload();
        if (payload == null)
            return defaultValue;
        Object value = payload.get(fieldName);
        if (value == null)
            return defaultValue;
        if (value instanceof Number n)
            return n.longValue();
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private void publishMessageEvent(Message message) {
        try {
            messageEventPublisher.publishMessageSent(message);
        } catch (Exception e) {
            log.error("Failed to publish message event: messageId={}", message.getId().value(), e);
        }
    }

    private MessageResponse convertToResponse(Message message) {
        String contentText = "";
        MessageContent content = message.getContent();
        if (content instanceof MessageContent.Text t) {
            contentText = t.text();
        } else if (content instanceof MessageContent.Image i) {
            contentText = "[Image] " + i.fileName();
        } else if (content instanceof MessageContent.File f) {
            contentText = "[File] " + f.fileName();
        }

        return new MessageResponse(
                message.getId().value(),
                message.getChannelId().value(),
                message.getSenderId().value(),
                message.getType(),
                contentText,
                message.getStatus(),
                message.getCreatedAt(),
                message.getSentAt(),
                message.getDeliveredAt(),
                message.getReadAt());
    }
}
