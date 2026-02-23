package com.example.chat.push.infrastructure.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * 푸시 처리 결과 Kafka 발행자
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PushResultProducer {
    private static final String TOPIC = "push-result-events";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendResult(PushResultEvent result) {
        try {
            String message = objectMapper.writeValueAsString(result);
            log.info("Publishing push result: {}", message);
            kafkaTemplate.send(TOPIC, message);
        } catch (Exception e) {
            log.error("Failed to publish push result for messageId={}", result.pushMessageId(), e);
        }
    }
}
