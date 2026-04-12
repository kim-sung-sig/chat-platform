package com.example.chat.ops.github.connector.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record GitHubTicketSyncRequest(
        @NotBlank String projectId,
        @NotBlank String title,
        String body,
        String assignee
) {
}
