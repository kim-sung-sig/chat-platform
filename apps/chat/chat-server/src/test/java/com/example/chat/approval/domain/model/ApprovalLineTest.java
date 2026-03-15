package com.example.chat.approval.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApprovalLineTest {

    @Test
    void lineNumberMustBeWithinRange() {
        Approver approver = new Approver(ApproverType.USER, "user-1");

        assertThrows(IllegalArgumentException.class, () -> new ApprovalLine(0, approver));
        assertThrows(IllegalArgumentException.class, () -> new ApprovalLine(7, approver));

        ApprovalLine line = new ApprovalLine(1, approver);
        assertEquals(1, line.getLineNumber());
    }

    @Test
    void approveMarksStatusAndDecisionTime() {
        ApprovalLine line = new ApprovalLine(1, new Approver(ApproverType.USER, "user-1"));

        line.markApproved();

        assertEquals(ApprovalStatus.APPROVED, line.getStatus());
        assertNotNull(line.getDecidedAt());
    }

    @Test
    void rejectMarksStatusAndDecisionTime() {
        ApprovalLine line = new ApprovalLine(1, new Approver(ApproverType.USER, "user-1"));

        line.markRejected();

        assertEquals(ApprovalStatus.REJECTED, line.getStatus());
        assertNotNull(line.getDecidedAt());
    }

    @Test
    void cannotDecideLineTwice() {
        ApprovalLine line = new ApprovalLine(1, new Approver(ApproverType.USER, "user-1"));

        line.markApproved();

        assertThrows(IllegalStateException.class, line::markApproved);
        assertThrows(IllegalStateException.class, line::markRejected);
    }
}
