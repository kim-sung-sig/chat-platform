
package com.example.chat.push.interfaces.rest

import com.example.chat.push.infrastructure.sender.SseSessionManager
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@RestController
@RequestMapping("/api/v1/push")
class SseController(
    private val sseSessionManager: SseSessionManager
) {
    @GetMapping("/stream/{userId}", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun stream(
        @PathVariable userId: String
    ): SseEmitter {
        val emitter = SseEmitter(30 * 60 * 1000L) // 30 mins timeout
        // Send initial connect event
        emitter.send(SseEmitter.event().name("connect").data("Connected for user: $userId"))
        sseSessionManager.add(userId, emitter)
        return emitter
    }
}
