package com.example.chat.approval.rest.dto.response;

import java.util.List;

public record ApprovalResponse(
        String id,
        String status,
        String createdBy,
        String createdAt,
        List<ApprovalLineResponse> lines
) {
    public record ApprovalLineResponse(
            Integer lineNumber,
            String approverType,
            String approverRefId,
            String status
    ) {}
}
