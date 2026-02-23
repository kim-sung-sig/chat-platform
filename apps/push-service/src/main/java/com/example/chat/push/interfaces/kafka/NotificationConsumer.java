package com.example.chat.push.interfaces.kafka;

import com.example.chat.push.application.PushMessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 알림 이벤트 Kafka 컨슈머
 *
 * 책임: notification-events 토픽을 구독하여 PushMessage를 저장합니다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {
    private final PushMessageService pushMessageService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "notification-events",
            groupId = "${spring.kafka.consumer.group-id:push-service-group}")
    public void consume(String message) {
        log.info("Consumed notification event: {}", message);
        try {
            NotificationEvent event = objectMapper.readValue(message, NotificationEvent.class);
            pushMessageService.savePushMessage(event);
        } catch (Exception e) {
            log.error("Failed to process notification event", e);
        }
    }
}
