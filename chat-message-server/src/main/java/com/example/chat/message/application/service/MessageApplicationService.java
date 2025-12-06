package com.example.chat.message.application.service;

import com.example.chat.common.auth.context.UserContextHolder;
import com.example.chat.common.auth.model.UserId;
import com.example.chat.message.application.dto.request.SendMessageRequest;
import com.example.chat.message.application.dto.response.MessageResponse;
import com.example.chat.message.domain.service.MessageDomainService;
import com.example.chat.message.infrastructure.messaging.MessageEventPublisher;
import com.example.chat.storage.domain.message.Message;
import com.example.chat.storage.factory.MessageFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 메시지 애플리케이션 서비스
 * Key 기반 도메인 조회 후 조립 패턴 적용
 * 얼리 리턴 패턴 적용
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageApplicationService {

    private final MessageFactory messageFactory;
    private final MessageDomainService messageDomainService;
    private final MessageEventPublisher messageEventPublisher;

    /**
     * 메시지 발송 (Key 기반 도메인 조회 후 조립 패턴)
     */
    @Transactional
    public MessageResponse sendMessage(SendMessageRequest request) {
        log.info("Sending message: roomId={}, type={}", request.getRoomId(), request.getMessageType());

        // Early return 1: 인증된 사용자 확인
        UserId senderId = UserContextHolder.getUserId();
        if (senderId == null) {
            throw new IllegalStateException("User not authenticated");
        }

        // Early return 2: Payload 검증
        if (request.getPayload() == null || request.getPayload().isEmpty()) {
            throw new IllegalArgumentException("Payload cannot be empty");
        }

        // Step 1: Key 기반 도메인 생성
        Message message = createMessageFromRequest(request, senderId);

        // Step 2: 도메인 서비스를 통한 비즈니스 로직 실행
        Message processedMessage = messageDomainService.processAndSave(message);

        // Step 3: 이벤트 발행 (비동기 처리)
        publishMessageEvent(processedMessage);

        // Step 4: Response 변환
        MessageResponse response = convertToResponse(processedMessage);

        log.info("Message sent successfully: messageId={}", processedMessage.getId());

        return response;
    }

    /**
     * 답장 메시지 발송
     */
    @Transactional
    public MessageResponse sendReplyMessage(SendMessageRequest request) {
        log.info("Sending reply message: roomId={}, replyTo={}",
            request.getRoomId(), request.getReplyToMessageId());

        // Early return 1: 인증된 사용자 확인
        UserId senderId = UserContextHolder.getUserId();
        if (senderId == null) {
            throw new IllegalStateException("User not authenticated");
        }

        // Early return 2: replyToMessageId 필수 확인
        if (request.getReplyToMessageId() == null) {
            throw new IllegalArgumentException("replyToMessageId is required for reply message");
        }

        // Step 1: 원본 메시지 조회 및 검증
        Message originalMessage = findAndValidateOriginalMessage(request.getReplyToMessageId());

        // Step 2: 답장 메시지 생성
        Message replyMessage = createReplyMessageFromRequest(request, senderId);

        // Step 3: 도메인 서비스를 통한 처리
        Message processedMessage = messageDomainService.processAndSave(replyMessage);

        // Step 4: 이벤트 발행
        publishMessageEvent(processedMessage);

        // Step 5: Response 변환
        MessageResponse response = convertToResponse(processedMessage);

        log.info("Reply message sent successfully: messageId={}, replyTo={}",
            processedMessage.getId(), request.getReplyToMessageId());

        return response;
    }

    // ========== Private Helper Methods (Key 기반 도메인 조회) ==========

    /**
     * Request에서 Message 도메인 생성 (일반 메시지)
     */
    private Message createMessageFromRequest(SendMessageRequest request, UserId senderId) {
        return messageFactory.createMessage(
            request.getRoomId(),
            request.getChannelId(),
            senderId,
            request.getMessageType(),
            request.getPayload()
        );
    }

    /**
     * Request에서 Message 도메인 생성 (답장 메시지)
     */
    private Message createReplyMessageFromRequest(SendMessageRequest request, UserId senderId) {
        return messageFactory.createReplyMessage(
            request.getRoomId(),
            request.getChannelId(),
            senderId,
            request.getMessageType(),
            request.getPayload(),
            request.getReplyToMessageId()
        );
    }

    /**
     * 원본 메시지 조회 및 검증
     */
    private Message findAndValidateOriginalMessage(Long messageId) {
        Message originalMessage = messageDomainService.findById(messageId);

        // Early return: 원본 메시지 존재 확인
        if (originalMessage == null) {
            throw new IllegalArgumentException(
                String.format("Original message not found: %d", messageId)
            );
        }

        // Early return: 삭제된 메시지 확인
        if (originalMessage.getIsDeleted()) {
            throw new IllegalStateException("Cannot reply to deleted message");
        }

        return originalMessage;
    }

    /**
     * 이벤트 발행
     */
    private void publishMessageEvent(Message message) {
        try {
            messageEventPublisher.publishMessageSent(message);
        } catch (Exception e) {
            log.error("Failed to publish message event: messageId={}", message.getId(), e);
            // 이벤트 발행 실패는 메시지 발송을 막지 않음 (보상 트랜잭션으로 처리 가능)
        }
    }

    /**
     * Message 도메인을 Response DTO로 변환
     */
    private MessageResponse convertToResponse(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .roomId(message.getRoomId())
                .channelId(message.getChannelId())
                .senderId(message.getSenderId().getValue())
                .messageType(message.getMessageType())
                .contentJson(message.getContent().toJson())
                .status(message.getStatus())
                .sentAt(message.getSentAt())
                .updatedAt(message.getUpdatedAt())
                .replyToMessageId(message.getReplyToMessageId())
                .isEdited(message.getIsEdited())
                .isDeleted(message.getIsDeleted())
                .build();
    }
}
