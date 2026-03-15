package com.example.chat.approval.domain.model;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ApprovalDocument {
    private final String id;
    private final String title;
    private final String content;
    private final String createdBy;
    private final Instant createdAt;
    private final List<ApprovalLine> lines;
    private ApprovalStatus status;

    public ApprovalDocument(String id, String title, String content, String createdBy, Instant createdAt, List<ApprovalLine> lines) {
        this.id = Objects.requireNonNull(id, "id");
        this.title = Objects.requireNonNull(title, "title");
        this.content = Objects.requireNonNull(content, "content");
        this.createdBy = Objects.requireNonNull(createdBy, "createdBy");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.lines = List.copyOf(Objects.requireNonNull(lines, "lines"));
        this.status = ApprovalStatus.DRAFT;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<ApprovalLine> getLines() {
        return Collections.unmodifiableList(lines);
    }

    public ApprovalStatus getStatus() {
        return status;
    }

    public void submit() {
        throw new UnsupportedOperationException("TODO: implement submit transition");
    }

    public void approveLine(int lineNumber) {
        throw new UnsupportedOperationException("TODO: implement line approval");
    }

    public void rejectLine(int lineNumber) {
        throw new UnsupportedOperationException("TODO: implement line rejection");
    }

    public void cancel() {
        throw new UnsupportedOperationException("TODO: implement cancel transition");
    }
}
