package com.example.chat.ops.approval.service.presentation;

import com.example.chat.common.web.response.ApiResponse;
import com.example.chat.ops.approval.service.application.ApprovalService;
import com.example.chat.ops.approval.service.domain.ApprovalRecord;
import com.example.chat.ops.contract.approval.ApprovalDecision;
import com.example.chat.ops.contract.rbac.OpsAction;
import com.example.chat.ops.contract.rbac.OpsAuthorization;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/plans")
public class ApprovalController {
    private final ApprovalService approvalService;

    public ApprovalController(ApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    @PostMapping("/{planId}/approve")
    public ResponseEntity<ApiResponse<ApprovalRecord>> approve(
            @RequestHeader("X-Project-Role") String role,
            @PathVariable String planId,
            @RequestBody(required = false) String reason
    ) {
        OpsAuthorization.require(role, OpsAction.PLAN_APPROVE);
        ApprovalRecord record = approvalService.decide(planId, ApprovalDecision.APPROVE, role, reason);
        return ApiResponse.ok(record).toResponseEntity();
    }

    @PostMapping("/{planId}/reject")
    public ResponseEntity<ApiResponse<ApprovalRecord>> reject(
            @RequestHeader("X-Project-Role") String role,
            @PathVariable String planId,
            @RequestBody(required = false) String reason
    ) {
        OpsAuthorization.require(role, OpsAction.PLAN_APPROVE);
        ApprovalRecord record = approvalService.decide(planId, ApprovalDecision.REJECT, role, reason);
        return ApiResponse.ok(record).toResponseEntity();
    }

    @PostMapping("/{planId}/request-change")
    public ResponseEntity<ApiResponse<ApprovalRecord>> requestChange(
            @RequestHeader("X-Project-Role") String role,
            @PathVariable String planId,
            @RequestBody(required = false) String reason
    ) {
        OpsAuthorization.require(role, OpsAction.PLAN_APPROVE);
        ApprovalRecord record = approvalService.decide(planId, ApprovalDecision.REQUEST_CHANGE, role, reason);
        return ApiResponse.ok(record).toResponseEntity();
    }
}
