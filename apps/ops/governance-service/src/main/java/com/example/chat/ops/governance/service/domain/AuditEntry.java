package com.example.chat.ops.governance.service.domain;

import java.time.Instant;

public record AuditEntry(
        String actor,
        String action,
        String projectId,
        String detail,
        Instant createdAt
) {
}
