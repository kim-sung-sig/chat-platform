package com.example.chat.ops.governance.service.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record CreatePolicyProposalRequest(
        @NotBlank String projectId,
        @NotBlank String title,
        @NotBlank String description,
        @NotBlank String prUrl
) {
}
