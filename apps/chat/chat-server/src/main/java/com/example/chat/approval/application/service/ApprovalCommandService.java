package com.example.chat.approval.application.service;

import com.example.chat.approval.domain.model.ApprovalDocument;
import com.example.chat.approval.domain.repository.ApprovalDocumentRepository;

import java.util.Objects;

public class ApprovalCommandService {
    private final ApprovalDocumentRepository repository;

    public ApprovalCommandService(ApprovalDocumentRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository");
    }

    public ApprovalDocument create(ApprovalDocument document) {
        return repository.save(document);
    }

    public void submit(String documentId) {
        throw new UnsupportedOperationException("TODO: implement submit command");
    }

    public void approve(String documentId, int lineNumber) {
        throw new UnsupportedOperationException("TODO: implement approve command");
    }

    public void reject(String documentId, int lineNumber) {
        throw new UnsupportedOperationException("TODO: implement reject command");
    }

    public void cancel(String documentId) {
        throw new UnsupportedOperationException("TODO: implement cancel command");
    }
}
