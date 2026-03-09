package com.example.chat.channel.infrastructure.redis;

import java.time.Instant;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 읽음 처리 완료 이벤트 Redis Pub/Sub 발행자
 *
 * 채널: chat:read:event:{channelId}
 * 구독자: websocket-server (ReadReceiptRedisSubscriber)
 * 역할: markAsRead 처리 완료 후 채널 멤버 전체에게 읽음 상태 브로드캐스트
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReadReceiptEventPublisher {

    private static final String READ_EVENT_CHANNEL_PREFIX = "chat:read:event:";

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 읽음 처리 이벤트 발행
     *
     * @param userId              읽음 처리한 사용자 ID
     * @param channelId           채널 ID
     * @param lastReadMessageId   마지막으로 읽은 메시지 ID
     */
    public void publish(String userId, String channelId, String lastReadMessageId) {
        try {
            ReadReceiptRedisPayload payload = new ReadReceiptRedisPayload(
                    "READ_RECEIPT", userId, channelId, lastReadMessageId, Instant.now());
            String json = objectMapper.writeValueAsString(payload);
            redisTemplate.convertAndSend(READ_EVENT_CHANNEL_PREFIX + channelId, json);
            log.debug("Read receipt event published: userId={}, channelId={}, messageId={}",
                    userId, channelId, lastReadMessageId);
        } catch (Exception e) {
            log.error("Failed to publish read receipt event: userId={}, channelId={}", userId, channelId, e);
        }
    }

    /**
     * Redis Pub/Sub으로 전송되는 읽음 이벤트 페이로드
     */
    public record ReadReceiptRedisPayload(
            String eventType,
            String userId,
            String channelId,
            String lastReadMessageId,
            Instant readAt) {
    }
}
