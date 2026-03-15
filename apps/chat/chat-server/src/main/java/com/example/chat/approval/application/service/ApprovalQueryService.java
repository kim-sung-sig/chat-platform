package com.example.chat.approval.application.service;

import com.example.chat.approval.domain.model.ApprovalDocument;
import com.example.chat.approval.domain.repository.ApprovalDocumentRepository;

import java.util.List;
import java.util.Objects;

public class ApprovalQueryService {
    private final ApprovalDocumentRepository repository;

    public ApprovalQueryService(ApprovalDocumentRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository");
    }

    public ApprovalDocument getById(String documentId) {
        throw new UnsupportedOperationException("TODO: implement get document status");
    }

    public List<ApprovalDocument> listInbox(String approverId) {
        throw new UnsupportedOperationException("TODO: implement list inbox");
    }
}
