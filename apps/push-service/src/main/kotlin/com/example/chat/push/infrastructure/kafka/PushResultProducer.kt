package com.example.chat.push.infrastructure.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class PushResultProducer(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper
) {

    fun sendResult(result: PushResultEvent) {
        val topic = "push-result-events"
        val message = objectMapper.writeValueAsString(result)
        logger.info { "Publishing push result: $message" }
        kafkaTemplate.send(topic, message)
    }
}

data class PushResultEvent(
    val pushMessageId: Long,
    val targetUserId: String,
    val status: String,
    val errorMessage: String? = null
)
