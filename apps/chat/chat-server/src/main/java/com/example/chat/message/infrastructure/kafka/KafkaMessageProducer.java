package com.example.chat.message.infrastructure.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

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
    private static final String TOPIC = "notification-events";

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
            kafkaTemplate.send(TOPIC, message);
            log.debug("Notification published: targetUserId={}, pushType={}", targetUserId, pushType);
        } catch (Exception e) {
            log.error("Failed to publish notification: targetUserId={}", targetUserId, e);
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
