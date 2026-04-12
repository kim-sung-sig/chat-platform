package com.example.chat.ops.governance.service.domain;

import java.time.Instant;

public record PolicyProposal(
        String proposalId,
        String projectId,
        String title,
        String description,
        String prUrl,
        String requestedBy,
        Instant requestedAt
) {
}
