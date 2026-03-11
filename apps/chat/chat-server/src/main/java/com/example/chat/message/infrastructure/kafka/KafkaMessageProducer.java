package com.example.chat.message.infrastructure.kafka;

import java.time.Instant;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.example.chat.channel.infrastructure.kafka.MemberLeftKafkaEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Kafka 메시지 발행자
 *
 * 책임: 메시지 이벤트를 push-service로 전달
 * - notification-events 토픽으로 발행
 * - push-service의 NotificationEvent 스키마 준수
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaMessageProducer {
    private static final String NOTIFICATION_TOPIC = "notification-events";
    private static final String READ_RECEIPT_TOPIC  = "read-receipt-events";
    private static final String MEMBER_LEFT_TOPIC   = "member-left-events";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 알림 이벤트 발행
     *
     * @param targetUserId 수신 대상 사용자 ID
     * @param title        알림 제목
     * @param content      알림 내용
     * @param pushType     푸시 타입 (예: CHAT_MESSAGE)
     */
    public void publishNotification(String targetUserId, String title, String content, String pushType) {
        NotificationEvent event = new NotificationEvent(targetUserId, title, content, pushType);
        try {
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(NOTIFICATION_TOPIC, message);
            log.debug("Notification published: targetUserId={}, pushType={}", targetUserId, pushType);
        } catch (Exception e) {
            log.error("Failed to publish notification: targetUserId={}", targetUserId, e);
        }
    }

    /**
     * 읽음 처리 이벤트 발행 (message.unread_count 비동기 감소용)
     *
     * @param event ReadReceiptKafkaEvent
     */
    public void publishReadReceipt(ReadReceiptKafkaEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            // channelId를 파티션 키로 사용 → 같은 채널 이벤트는 순서 보장
            kafkaTemplate.send(READ_RECEIPT_TOPIC, event.channelId(), message);
            log.debug("ReadReceipt event published: userId={}, channelId={}", event.userId(), event.channelId());
        } catch (Exception e) {
            log.error("Failed to publish read-receipt event: userId={}, channelId={}", event.userId(), event.channelId(), e);
        }
    }

    /**
     * 멤버 퇴장 이벤트 발행 (미읽음 message.unread_count 보정용)
     *
     * @param userId    퇴장한 사용자 ID
     * @param channelId 채널 ID
     * @param lastReadAt 마지막 읽음 기준 시각 (null = 한 번도 읽지 않음)
     */
    public void publishMemberLeft(String userId, String channelId, Instant lastReadAt) {
        try {
            MemberLeftKafkaEvent event = new MemberLeftKafkaEvent(userId, channelId, lastReadAt);
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(MEMBER_LEFT_TOPIC, channelId, message);
            log.debug("MemberLeft event published: userId={}, channelId={}", userId, channelId);
        } catch (Exception e) {
            log.error("Failed to publish member-left event: userId={}, channelId={}", userId, channelId, e);
        }
    }

    /**
     * 알림 이벤트 DTO
     * push-service의 NotificationEvent와 동일한 구조
     */
    public record NotificationEvent(
            String targetUserId,
            String title,
            String content,
            String pushType) {
    }
}
