
package com.example.chat.push.infrastructure.sender

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

private val logger = KotlinLogging.logger {}

@Component
class TeamsPushSender(
    private val restTemplate: RestTemplate
) : PushSender {
    override fun support(pushType: String): Boolean = pushType.uppercase() == "TEAMS"

    override fun send(
        targetUserId: String,
        title: String,
        content: String
    ) {
        // In a real system, you'd fetch the Microsoft Teams Webhook URL
        val webhookUrl = "https://outlook.office.com/webhook/..."

        val payload = mapOf(
            "type" to "message",
            "attachments" to listOf(
                mapOf(
                    "contentType" to "application/vnd.microsoft.card.adaptive",
                    "content" to mapOf(
                        "type" to "AdaptiveCard",
                        "body" to listOf(
                            mapOf("type" to "TextBlock", "text" to title, "weight" to "bolder", "size" to "medium"),
                            mapOf("type" to "TextBlock", "text" to content, "wrap" to true)
                        ),
                        "\$schema" to "http://adaptivecards.io/schemas/adaptive-card.json",
                        "version" to "1.0"
                    )
                )
            )
        )

        logger.info { "Sending Teams push to user $targetUserId" }
        // Simulating the call
        // restTemplate.postForEntity(webhookUrl, payload, String::class.java)
        
        logger.info { "Teams payload: $payload" }
    }
}
