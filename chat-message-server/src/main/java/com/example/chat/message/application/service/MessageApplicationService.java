package com.example.chat.message.application.service;

import com.example.chat.common.auth.context.UserContextHolder;
import com.example.chat.domain.channel.ChannelId;
import com.example.chat.domain.message.Message;
import com.example.chat.domain.message.MessageRepository;
import com.example.chat.domain.message.MessageType;
import com.example.chat.domain.service.MessageDomainService;
import com.example.chat.domain.user.UserId;
import com.example.chat.message.application.dto.request.SendMessageRequest;
import com.example.chat.message.application.dto.response.MessageResponse;
import com.example.chat.message.infrastructure.messaging.MessageEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 메시지 애플리케이션 서비스 (Use Case)
 * - Early Return 패턴 적용
 * - Key 기반 도메인 조회 패턴
 * - Domain Service 활용
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageApplicationService {

    private final MessageRepository messageRepository;
    private final MessageDomainService messageDomainService;
    private final MessageEventPublisher messageEventPublisher;

    /**
     * 메시지 발송 Use Case
     */
    @Transactional
    public MessageResponse sendMessage(SendMessageRequest request) {
        log.info("Sending message: channelId={}, type={}", request.getChannelId(), request.getMessageType());

        // Early return 1: 인증된 사용자 확인
        com.example.chat.common.auth.model.UserId authUserId = UserContextHolder.getUserId();
        if (authUserId == null) {
            throw new IllegalStateException("User not authenticated");
        }
        UserId senderId = UserId.of(String.valueOf(authUserId.getValue()));

        // Early return 2: 필수 파라미터 검증
        if (request.getChannelId() == null || request.getChannelId().isBlank()) {
            throw new IllegalArgumentException("Channel ID is required");
        }

        // Step 1: ChannelId로 도메인 객체 생성
        ChannelId channelId = ChannelId.of(request.getChannelId());

        // Step 2: MessageType에 따라 Domain Service로 메시지 생성
        Message message = createMessageByType(channelId, senderId, request);

        // Step 3: 저장
        Message savedMessage = messageRepository.save(message);

        // Step 4: 이벤트 발행 (비동기)
        publishMessageEvent(savedMessage);

        // Step 5: Response 변환
        MessageResponse response = convertToResponse(savedMessage);

        log.info("Message sent successfully: messageId={}", savedMessage.getId().getValue());

        return response;
    }

    // ========== Private Helper Methods ==========

    /**
     * MessageType에 따라 메시지 생성
     */
    private Message createMessageByType(ChannelId channelId, UserId senderId, SendMessageRequest request) {
        MessageType type = request.getMessageType();

        // Early return: MessageType 검증
        if (type == null) {
            throw new IllegalArgumentException("Message type is required");
        }

        switch (type) {
            case TEXT:
                String text = extractTextField(request, "text");
                return messageDomainService.createTextMessage(channelId, senderId, text);

            case IMAGE:
                String imageUrl = extractTextField(request, "imageUrl");
                String imageName = extractTextFieldOrDefault(request, "fileName", "image.jpg");
                Long imageSize = extractLongFieldOrDefault(request, "fileSize", 0L);
                return messageDomainService.createImageMessage(channelId, senderId, imageUrl, imageName, imageSize);

            case FILE:
                String fileUrl = extractTextField(request, "fileUrl");
                String fileName = extractTextField(request, "fileName");
                Long fileSize = extractLongFieldOrDefault(request, "fileSize", 0L);
                String mimeType = extractTextFieldOrDefault(request, "mimeType", "application/octet-stream");
                return messageDomainService.createFileMessage(channelId, senderId, fileUrl, fileName, fileSize, mimeType);

            case SYSTEM:
                String systemText = extractTextField(request, "text");
                return messageDomainService.createSystemMessage(channelId, systemText);

            default:
                throw new IllegalArgumentException("Unsupported message type: " + type);
        }
    }

    /**
     * Payload에서 필수 텍스트 필드 추출
     */
    private String extractTextField(SendMessageRequest request, String fieldName) {
        if (request.getPayload() == null) {
            throw new IllegalArgumentException("Payload is required");
        }

        Object value = request.getPayload().get(fieldName);
        if (value == null) {
            throw new IllegalArgumentException(String.format("Field '%s' is required in payload", fieldName));
        }

        return value.toString();
    }

    /**
     * Payload에서 텍스트 필드 추출 (기본값 있음)
     */
    private String extractTextFieldOrDefault(SendMessageRequest request, String fieldName, String defaultValue) {
        if (request.getPayload() == null) {
            return defaultValue;
        }

        Object value = request.getPayload().get(fieldName);
        return value != null ? value.toString() : defaultValue;
    }

    /**
     * Payload에서 Long 필드 추출 (기본값 있음)
     */
    private Long extractLongFieldOrDefault(SendMessageRequest request, String fieldName, Long defaultValue) {
        if (request.getPayload() == null) {
            return defaultValue;
        }

        Object value = request.getPayload().get(fieldName);
        if (value == null) {
            return defaultValue;
        }

        if (value instanceof Number) {
            return ((Number) value).longValue();
        }

        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 이벤트 발행
     */
    private void publishMessageEvent(Message message) {
        try {
            messageEventPublisher.publishMessageSent(message);
        } catch (Exception e) {
            log.error("Failed to publish message event: messageId={}", message.getId().getValue(), e);
            // 이벤트 발행 실패는 메시지 발송을 막지 않음
        }
    }

    /**
     * Message 도메인을 Response DTO로 변환
     */
    private MessageResponse convertToResponse(Message message) {
        return MessageResponse.builder()
                .id(message.getId().getValue())
                .channelId(message.getChannelId().getValue())
                .senderId(message.getSenderId().getValue())
                .messageType(message.getType())
                .content(message.getContent().getText())  // 간단히 text만 반환
                .status(message.getStatus())
                .createdAt(message.getCreatedAt())
                .sentAt(message.getSentAt())
                .build();
    }
}
