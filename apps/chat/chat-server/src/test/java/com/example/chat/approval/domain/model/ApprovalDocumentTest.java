package com.example.chat.approval.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ApprovalDocumentTest {

    @Test
    void submitTransitionsToInReview() {
        ApprovalDocument doc = sampleDocument();

        doc.submit();

        assertEquals(ApprovalStatus.IN_REVIEW, doc.getStatus());
    }

    @Test
    void approveAllLinesTransitionsToApproved() {
        ApprovalDocument doc = sampleDocument();

        doc.submit();
        doc.approveLine(1);

        assertEquals(ApprovalStatus.APPROVED, doc.getStatus());
    }

    @Test
    void rejectAnyLineTransitionsToRejected() {
        ApprovalDocument doc = sampleDocument();

        doc.submit();
        doc.rejectLine(1);

        assertEquals(ApprovalStatus.REJECTED, doc.getStatus());
    }

    @Test
    void cancelBeforeFinalizationIsAllowed() {
        ApprovalDocument doc = sampleDocument();

        doc.cancel();

        assertEquals(ApprovalStatus.CANCELED, doc.getStatus());
    }

    @Test
    void cannotCancelAfterFinalization() {
        ApprovalDocument doc = sampleDocument();

        doc.submit();
        doc.approveLine(1);

        assertThrows(IllegalStateException.class, doc::cancel);
    }

    private ApprovalDocument sampleDocument() {
        ApprovalLine line = new ApprovalLine(1, new Approver(ApproverType.USER, "user-1"));
        return new ApprovalDocument(
                "doc-1",
                "title",
                "content",
                "creator-1",
                Instant.now(),
                List.of(line)
        );
    }
}
