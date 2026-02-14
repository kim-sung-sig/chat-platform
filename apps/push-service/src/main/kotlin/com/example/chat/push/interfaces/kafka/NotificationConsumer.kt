package com.example.chat.push.interfaces.kafka

import com.example.chat.push.application.PushMessageService
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger {}

@Component
class NotificationConsumer(
        private val pushMessageService: PushMessageService,
        private val objectMapper: ObjectMapper
) {
    @KafkaListener(
        topics = ["notification-events"],
        groupId = "\${spring.kafka.consumer.group-id:push-service-group}"
    )
    fun consume(message: String) {
        log.info { "Consumed notification event: $message" }
        try {
            val event = objectMapper.readValue(message, NotificationEvent::class.java)
            pushMessageService.savePushMessage(event)
        } catch (e: Exception) {
            log.error(e) { "Failed to process notification event" }
        }
    }
}

data class NotificationEvent(
        val targetUserId: String,
        val title: String,
        val content: String,
        val pushType: String
)
