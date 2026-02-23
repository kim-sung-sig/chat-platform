package com.example.chat.push.infrastructure.sender;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Microsoft Teams 푸시 발송자
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TeamsPushSender implements PushSender {
    private final RestTemplate restTemplate;

    @Override
    public boolean support(String pushType) {
        return "TEAMS".equalsIgnoreCase(pushType);
    }

    @Override
    public void send(String targetUserId, String title, String content) {
        String webhookUrl = "https://outlook.office.com/webhook/...";

        Map<String, Object> payload = Map.of(
                "type", "message",
                "attachments", List.of(Map.of(
                        "contentType", "application/vnd.microsoft.card.adaptive",
                        "content", Map.of(
                                "type", "AdaptiveCard",
                                "body", List.of(
                                        Map.of("type", "TextBlock", "text", title, "weight", "bolder", "size", "medium"),
                                        Map.of("type", "TextBlock", "text", content, "wrap", true)
                                ),
                                "$schema", "http://adaptivecards.io/schemas/adaptive-card.json",
                                "version", "1.0"
                        )
                ))
        );

        log.info("Sending Teams push to user {}", targetUserId);
        // restTemplate.postForEntity(webhookUrl, payload, String.class);
        log.info("Teams payload: {}", payload);
    }
}
