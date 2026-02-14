package com.example.chat.push.application

import com.example.chat.push.domain.PushMessage
import com.example.chat.push.domain.PushMessageRepository
import com.example.chat.push.domain.PushStatus
import com.example.chat.push.infrastructure.kafka.PushResultEvent
import com.example.chat.push.infrastructure.kafka.PushResultProducer
import com.example.chat.push.infrastructure.sender.PushSender
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

@Service
class PushProcessor(
    private val repository: PushMessageRepository,
    private val senders: List<PushSender>,
    private val pushResultProducer: PushResultProducer
) {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun process(message: PushMessage) {
        logger.info { "Processing push message: ${message.id}" }
        
        try {
            // Mark as processing
            message.status = PushStatus.PROCESSING
            repository.saveAndFlush(message)

            val sender = senders.find { it.support(message.pushType) }
                ?: throw IllegalArgumentException("No sender found for type: ${message.pushType}")

            sender.send(message.targetUserId, message.title, message.content)

            message.status = PushStatus.COMPLETED
            message.processedAt = LocalDateTime.now()
        } catch (e: Exception) {
            logger.error(e) { "Failed to send push message: ${message.id}" }
            message.status = PushStatus.FAILED
            message.errorMessage = e.message
        } finally {
            repository.save(message)
            
            // Republish result
            try {
                pushResultProducer.sendResult(
                    PushResultEvent(
                        pushMessageId = message.id!!,
                        targetUserId = message.targetUserId,
                        status = message.status.name,
                        errorMessage = message.errorMessage
                    )
                )
            } catch (e: Exception) {
                logger.error(e) { "Failed to republish push result for message ${message.id}" }
            }
        }
    }
}
