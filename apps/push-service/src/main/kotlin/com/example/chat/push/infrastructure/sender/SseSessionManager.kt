
package com.example.chat.push.infrastructure.sender

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger {}

@Component
class SseSessionManager {
    private val emitters = ConcurrentHashMap<String, MutableList<SseEmitter>>()

    fun add(
        userId: String,
        emitter: SseEmitter
    ) {
        emitters.computeIfAbsent(userId) { mutableListOf() }.add(emitter)
        
        emitter.onCompletion { remove(userId, emitter) }
        emitter.onTimeout { remove(userId, emitter) }
        emitter.onError { remove(userId, emitter) }
        
        logger.info { "Added SSE emitter for user $userId. Active users: ${emitters.size}" }
    }

    fun remove(
        userId: String,
        emitter: SseEmitter
    ) {
        emitters[userId]?.remove(emitter)
        if (emitters[userId]?.isEmpty() == true) {
            emitters.remove(userId)
        }
    }

    fun send(
        userId: String,
        title: String,
        content: String
    ) {
        val userEmitters = emitters[userId] ?: return
        
        val deadEmitters = mutableListOf<SseEmitter>()
        
        userEmitters.forEach { emitter ->
            try {
                emitter.send(
                    SseEmitter.event()
                        .name("push")
                        .data(mapOf("title" to title, "content" to content))
                )
            } catch (e: Exception) {
                deadEmitters.add(emitter)
            }
        }
        
        deadEmitters.forEach { remove(userId, it) }
    }
}
