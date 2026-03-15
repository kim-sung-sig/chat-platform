package com.example.chat.approval.rest.dto.response;

import java.util.List;

public record ApprovalInboxResponse(
        List<ApprovalResponse> items
) {}
