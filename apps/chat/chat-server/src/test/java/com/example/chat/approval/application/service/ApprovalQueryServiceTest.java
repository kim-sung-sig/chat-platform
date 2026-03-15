package com.example.chat.approval.application.service;

import com.example.chat.approval.domain.model.*;
import com.example.chat.approval.domain.repository.ApprovalDocumentRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ApprovalQueryServiceTest {

    @Test
    void getByIdReturnsDocument() {
        ApprovalDocumentRepository repository = mock(ApprovalDocumentRepository.class);
        ApprovalQueryService service = new ApprovalQueryService(repository);

        ApprovalDocument doc = sampleDocument();
        when(repository.findById("doc-1")).thenReturn(Optional.of(doc));

        ApprovalDocument result = service.getById("doc-1");

        assertEquals("doc-1", result.getId());
    }

    @Test
    void listInboxReturnsItems() {
        ApprovalDocumentRepository repository = mock(ApprovalDocumentRepository.class);
        ApprovalQueryService service = new ApprovalQueryService(repository);

        ApprovalDocument doc = sampleDocument();
        when(repository.findInboxByApprover("user-1")).thenReturn(List.of(doc));

        assertEquals(1, service.listInbox("user-1").size());
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
