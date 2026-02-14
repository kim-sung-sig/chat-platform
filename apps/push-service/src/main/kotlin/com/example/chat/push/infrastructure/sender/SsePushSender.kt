
package com.example.chat.push.infrastructure.sender

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class SsePushSender(
    private val sseSessionManager: SseSessionManager
) : PushSender {
    override fun support(pushType: String): Boolean = pushType.uppercase() == "TOAST"

    override fun send(
        targetUserId: String,
        title: String,
        content: String
    ) {
        logger.info { "Pushing TOAST message to user $targetUserId via SSE" }
        sseSessionManager.send(targetUserId, title, content)
    }
}
