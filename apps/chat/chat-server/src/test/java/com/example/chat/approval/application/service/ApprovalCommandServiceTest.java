package com.example.chat.approval.application.service;

import com.example.chat.approval.domain.model.*;
import com.example.chat.approval.domain.repository.ApprovalDocumentRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ApprovalCommandServiceTest {

    @Test
    void submitPersistsInReviewState() {
        ApprovalDocumentRepository repository = mock(ApprovalDocumentRepository.class);
        ApprovalCommandService service = new ApprovalCommandService(repository);

        ApprovalDocument doc = sampleDocument();
        when(repository.findById("doc-1")).thenReturn(Optional.of(doc));

        service.submit("doc-1");

        ArgumentCaptor<ApprovalDocument> captor = ArgumentCaptor.forClass(ApprovalDocument.class);
        verify(repository).save(captor.capture());
        assertEquals(ApprovalStatus.IN_REVIEW, captor.getValue().getStatus());
    }

    @Test
    void approvePersistsApprovedState() {
        ApprovalDocumentRepository repository = mock(ApprovalDocumentRepository.class);
        ApprovalCommandService service = new ApprovalCommandService(repository);

        ApprovalDocument doc = sampleDocument();
        doc.submit();
        when(repository.findById("doc-1")).thenReturn(Optional.of(doc));

        service.approve("doc-1", 1);

        verify(repository).save(doc);
        assertEquals(ApprovalStatus.APPROVED, doc.getStatus());
    }

    @Test
    void rejectPersistsRejectedState() {
        ApprovalDocumentRepository repository = mock(ApprovalDocumentRepository.class);
        ApprovalCommandService service = new ApprovalCommandService(repository);

        ApprovalDocument doc = sampleDocument();
        doc.submit();
        when(repository.findById("doc-1")).thenReturn(Optional.of(doc));

        service.reject("doc-1", 1);

        verify(repository).save(doc);
        assertEquals(ApprovalStatus.REJECTED, doc.getStatus());
    }

    @Test
    void cancelPersistsCanceledState() {
        ApprovalDocumentRepository repository = mock(ApprovalDocumentRepository.class);
        ApprovalCommandService service = new ApprovalCommandService(repository);

        ApprovalDocument doc = sampleDocument();
        when(repository.findById("doc-1")).thenReturn(Optional.of(doc));

        service.cancel("doc-1");

        verify(repository).save(doc);
        assertEquals(ApprovalStatus.CANCELED, doc.getStatus());
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
