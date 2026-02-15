package com.example.chat.message.infrastructure.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.time.Instant

/**
 * Kafka 메시지 발행자
 *
 * 책임: 메시지 이벤트를 push-service로 전달
 * - notification-events 토픽으로 발행
 * - push-service의 NotificationEvent 스키마 준수
 */
@Component
class KafkaMessageProducer(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper
) {
    companion object {
        private const val TOPIC = "notification-events"
    }

    /**
     * 알림 이벤트 발행
     *
     * @param targetUserId 수신 대상 사용자 ID
     * @param title 알림 제목
     * @param content 알림 내용
     * @param pushType 푸시 타입 (예: CHAT_MESSAGE)
     */
    fun publishNotification(
        targetUserId: String,
        title: String,
        content: String,
        pushType: String = "CHAT_MESSAGE"
    ) {
        val event = NotificationEvent(
            targetUserId = targetUserId,
            title = title,
            content = content,
            pushType = pushType
        )

        val message = objectMapper.writeValueAsString(event)
        kafkaTemplate.send(TOPIC, message)
    }
}

/**
 * 알림 이벤트 DTO
 * push-service의 NotificationEvent와 동일한 구조
 */
data class NotificationEvent(
    val targetUserId: String,
    val title: String,
    val content: String,
    val pushType: String
)

