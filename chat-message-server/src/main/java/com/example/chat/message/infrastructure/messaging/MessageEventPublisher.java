package com.example.chat.message.infrastructure.messaging;

import com.example.chat.storage.domain.message.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 메시지 이벤트 발행자 (Redis Pub/Sub)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageEventPublisher {

    private static final String MESSAGE_SENT_CHANNEL_PREFIX = "chat:message:sent:";
    private static final String ROOM_CHANNEL_PREFIX = "chat:room:";

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 메시지 발송 이벤트 발행
     *
     * @param message 발송된 메시지
     */
    public void publishMessageSent(Message message) {
        // Early return: null 체크
        if (message == null) {
            log.warn("Cannot publish null message");
            return;
        }

        // Early return: 메시지 ID 체크
        if (message.getId() == null) {
            log.warn("Cannot publish message without ID");
            return;
        }

        try {
            // Step 1: 메시지 이벤트 DTO 생성
            MessageSentEvent event = createMessageSentEvent(message);

            // Step 2: JSON 직렬화
            String eventJson = serializeEvent(event);

            // Step 3: Redis Pub/Sub 발행 (채팅방 채널)
            publishToRoomChannel(message.getRoomId(), eventJson);

            log.info("Message event published: messageId={}, roomId={}",
                message.getId(), message.getRoomId());

        } catch (Exception e) {
            log.error("Failed to publish message event: messageId={}", message.getId(), e);
            throw new RuntimeException("Failed to publish message event", e);
        }
    }

    // ========== Private Helper Methods ==========

    /**
     * MessageSentEvent 생성
     */
    private MessageSentEvent createMessageSentEvent(Message message) {
        return MessageSentEvent.builder()
                .messageId(message.getId())
                .roomId(message.getRoomId())
                .channelId(message.getChannelId())
                .senderId(message.getSenderId().getValue())
                .messageType(message.getMessageType().getCode())
                .contentJson(message.getContent().toJson())
                .status(message.getStatus().getCode())
                .sentAt(message.getSentAt())
                .replyToMessageId(message.getReplyToMessageId())
                .build();
    }

    /**
     * JSON 직렬화
     */
    private String serializeEvent(MessageSentEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize message event", e);
        }
    }

    /**
     * 채팅방 채널로 발행
     */
    private void publishToRoomChannel(String roomId, String eventJson) {
        String channel = ROOM_CHANNEL_PREFIX + roomId;
        redisTemplate.convertAndSend(channel, eventJson);

        log.debug("Published to channel: {}", channel);
    }
}
