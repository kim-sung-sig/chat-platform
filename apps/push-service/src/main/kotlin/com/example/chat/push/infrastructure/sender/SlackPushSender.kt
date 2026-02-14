package com.example.chat.push.infrastructure.sender

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

private val logger = KotlinLogging.logger {}

@Component
class SlackPushSender(private val restTemplate: RestTemplate) : PushSender {
    override fun support(pushType: String): Boolean = pushType.uppercase() == "SLACK"

    override fun send(targetUserId: String, title: String, content: String) {
        // In a real system, you'd fetch the user's Slack Webhook URL from a database or service
        val webhookUrl = "https://hooks.slack.com/services/..."

        val payload = mapOf("text" to "*$title*\n$content")

        logger.info { "Sending Slack push to user $targetUserId via $webhookUrl" }

        // Simulating the call (commented out to avoid real 404/errors in background)
        // restTemplate.postForEntity(webhookUrl, payload, String::class.java)

        logger.info { "Slack payload: $payload" }
    }
}
