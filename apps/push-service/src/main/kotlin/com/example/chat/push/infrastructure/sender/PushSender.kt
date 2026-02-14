package com.example.chat.push.infrastructure.sender

interface PushSender {
    fun support(pushType: String): Boolean
    fun send(targetUserId: String, title: String, content: String)
}

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class ToastPushSender : PushSender {
    override fun support(pushType: String): Boolean = pushType == "TOAST"

    override fun send(targetUserId: String, title: String, content: String) {
        // Logic for Toast popup - e.g., Sending to WebSocket server or specific Kafka topic
        logger.info { "Sending TOAST push to user $targetUserId: $title - $content" }
    }
}

@Component
class ExternalPushSender : PushSender {
    override fun support(pushType: String): Boolean = pushType == "EXTERNAL"

    override fun send(targetUserId: String, title: String, content: String) {
        // Logic for External messaging system (FCM, Slack, etc.)
        logger.info { "Sending EXTERNAL push to user $targetUserId: $title - $content" }
    }
}
