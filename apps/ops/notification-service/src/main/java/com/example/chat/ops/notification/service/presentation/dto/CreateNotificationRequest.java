package com.example.chat.ops.notification.service.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateNotificationRequest(
        @NotBlank String projectId,
        @NotBlank String category,
        @NotBlank String message,
        String targetUserId
) {
}
