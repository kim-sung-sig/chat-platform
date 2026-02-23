package com.example.chat.push.infrastructure.sender;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Slack 푸시 발송자
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SlackPushSender implements PushSender {
    private final RestTemplate restTemplate;

    @Override
    public boolean support(String pushType) {
        return "SLACK".equalsIgnoreCase(pushType);
    }

    @Override
    public void send(String targetUserId, String title, String content) {
        String webhookUrl = "https://hooks.slack.com/services/...";
        Map<String, String> payload = Map.of("text", "*" + title + "*\n" + content);

        log.info("Sending Slack push to user {} via {}", targetUserId, webhookUrl);
        // restTemplate.postForEntity(webhookUrl, payload, String.class);
        log.info("Slack payload: {}", payload);
    }
}
