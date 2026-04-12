package com.example.chat.ops.approval.service.domain;

import com.example.chat.ops.contract.approval.ApprovalDecision;
import java.time.Instant;

public record ApprovalRecord(
        String planId,
        ApprovalDecision decision,
        String decidedBy,
        Instant decidedAt,
        String reason
) {
}
