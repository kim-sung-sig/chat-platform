package com.example.chat.push.application

import com.example.chat.push.domain.PushMessageRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.LocalDateTime
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {}

@Component
class PushOutboxScheduler(
        private val repository: PushMessageRepository,
        private val processor: PushProcessor
) {
    @Scheduled(fixedDelayString = "\${push.scheduler.delay:5000}")
    @Transactional
    fun schedule() {
        // Find pending messages older than 1 second to avoid race conditions with initial save
        val cutoff = LocalDateTime.now().minusSeconds(1)
        val pendingMessages = repository.findPendingForProcessing(cutoff)

        if (pendingMessages.isNotEmpty()) {
            logger.info { "Found ${pendingMessages.size} pending push messages" }
        }

        pendingMessages.forEach { message ->
            try {
                processor.process(message)
            } catch (e: Exception) {
                logger.error(e) { "Error delegating push message ${message.id} to processor" }
            }
        }
    }
}
