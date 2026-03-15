package com.example.chat.approval.domain.repository;

import com.example.chat.approval.domain.model.ApprovalDocument;

import java.util.List;
import java.util.Optional;

public interface ApprovalDocumentRepository {
    ApprovalDocument save(ApprovalDocument document);

    Optional<ApprovalDocument> findById(String id);

    List<ApprovalDocument> findInboxByApprover(String approverId);
}
