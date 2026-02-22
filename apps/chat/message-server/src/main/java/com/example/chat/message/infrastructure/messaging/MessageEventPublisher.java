package com.example.chat.message.infrastructure.messaging;

import com.example.chat.domain.message.Message;
import com.example.chat.domain.message.MessageContent;
import com.example.chat.domain.message.MessageId;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 메시지 이벤트 발행자 (Redis Pub/Sub)
 */
@Component
public class MessageEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(MessageEventPublisher.class);
    private static final String MESSAGE_SENT_CHANNEL_PREFIX = "chat:room:";

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public MessageEventPublisher(RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 메시지 발송 이벤트 발행
     */
    public void publishMessageSent(Message message) {
        MessageId messageId = message.getId();
        if (messageId == null) {
            log.warn("Cannot publish message without ID");
            return;
        }

        try {
            // Step 1: 메시지 이벤트 DTO 생성
            MessageSentEvent event = createMessageSentEvent(message);

            // Step 2: JSON 직렬화
            String eventJson = serializeEvent(event);

            // Step 3: Redis Pub/Sub 발행 (채널)
            publishToChannel(message.getChannelId().value(), eventJson);

            log.info("Message event published: messageId={}, channelId={}",
                    messageId.value(), message.getChannelId().value());
        } catch (Exception e) {
            log.error("Failed to publish message event: messageId={}", messageId.value(), e);
            throw new RuntimeException("Failed to publish message event", e);
        }
    }

    /**
     * MessageSentEvent 생성
     */
    private MessageSentEvent createMessageSentEvent(Message message) {
        String contentText = "";
        MessageContent content = message.getContent();
        if (content instanceof MessageContent.Text t) {
            contentText = t.text();
        } else if (content instanceof MessageContent.Image i) {
            contentText = "[Image] " + i.fileName();
        } else if (content instanceof MessageContent.File f) {
            contentText = "[File] " + f.fileName();
        }

        return new MessageSentEvent(
                message.getId().value(),
                message.getChannelId().value(),
                message.getSenderId().value(),
                message.getType().name(),
                contentText,
                message.getStatus().name(),
                message.getSentAt());
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
     * 채널로 발행
     */
    private void publishToChannel(String channelId, String eventJson) {
        String channel = MESSAGE_SENT_CHANNEL_PREFIX + channelId;
        redisTemplate.convertAndSend(channel, eventJson);
        log.debug("Published to channel: {}", channel);
    }
}
