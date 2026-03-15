package com.example.chat.approval.domain.model;

import java.time.Instant;
import java.util.Objects;

public class ApprovalLine {
    public static final int MIN_LINE = 1;
    public static final int MAX_LINE = 6;

    private final int lineNumber;
    private final Approver approver;
    private ApprovalStatus status;
    private Instant decidedAt;

    public ApprovalLine(int lineNumber, Approver approver) {
        this.lineNumber = lineNumber;
        this.approver = Objects.requireNonNull(approver, "approver");
        this.status = ApprovalStatus.DRAFT;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public Approver getApprover() {
        return approver;
    }

    public ApprovalStatus getStatus() {
        return status;
    }

    public Instant getDecidedAt() {
        return decidedAt;
    }

    public void markApproved() {
        throw new UnsupportedOperationException("TODO: implement approval transition");
    }

    public void markRejected() {
        throw new UnsupportedOperationException("TODO: implement reject transition");
    }
}
