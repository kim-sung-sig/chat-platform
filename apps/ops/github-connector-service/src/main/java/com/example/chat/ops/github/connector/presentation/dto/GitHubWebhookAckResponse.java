package com.example.chat.ops.github.connector.presentation.dto;

public record GitHubWebhookAckResponse(
        String deliveryId,
        String eventType,
        boolean accepted
) {
}
