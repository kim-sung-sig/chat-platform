package com.example.chat.message.infrastructure.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.example.chat.storage.entity.ChatMessageEntity;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 메시지 이벤트 발행자 (Redis Pub/Sub)
 *
 * Phase 4: Domain POJO 의존 제거 - Entity 기반 단일 발행 메서드만 유지
 */
@Component
public class MessageEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(MessageEventPublisher.class);
    private static final String CHANNEL_PREFIX = "chat:room:";

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public MessageEventPublisher(RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishMessageSent(ChatMessageEntity entity, int memberCount) {
        if (entity.getId() == null) {
            log.warn("Cannot publish message without ID");
            return;
        }
        try {
            String content = switch (entity.getMessageType()) {
                case TEXT, SYSTEM       -> entity.getContentText() != null ? entity.getContentText() : "";
                case IMAGE              -> "[Image] " + entity.getContentFileName();
                case FILE, VIDEO, AUDIO -> "[File] " + entity.getContentFileName();
            };
            int unreadCount = Math.max(0, memberCount - 1);
            publish(entity.getChannelId(), new MessageSentEvent(
                    "MESSAGE",
                    entity.getId(), entity.getChannelId(), entity.getSenderId(),
                    entity.getMessageType().name(), content,
                    entity.getMessageStatus().name(), unreadCount, entity.getSentAt()));
            log.info("Message event published: messageId={}, unreadCount={}", entity.getId(), unreadCount);
        } catch (Exception e) {
            log.error("Failed to publish message event: messageId={}", entity.getId(), e);
            throw new RuntimeException("Failed to publish message event", e);
        }
    }

    private void publish(String channelId, MessageSentEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            redisTemplate.convertAndSend(CHANNEL_PREFIX + channelId, json);
            log.debug("Published to channel: {}", CHANNEL_PREFIX + channelId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize/publish message event", e);
        }
    }
}
