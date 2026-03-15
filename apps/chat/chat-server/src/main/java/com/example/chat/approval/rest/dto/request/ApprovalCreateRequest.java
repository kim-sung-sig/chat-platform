package com.example.chat.approval.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ApprovalCreateRequest(
        @NotBlank(message = "Title is required") String title,
        @NotBlank(message = "Content is required") String content,
        @NotEmpty(message = "Lines are required") List<ApprovalLineRequest> lines
) {
    public record ApprovalLineRequest(
            @NotNull(message = "Line number is required") Integer lineNumber,
            @NotBlank(message = "Approver type is required") String approverType,
            @NotBlank(message = "Approver refId is required") String approverRefId
    ) {}
}
