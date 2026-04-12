package com.example.chat.ops.governance.service.presentation;

import com.example.chat.common.web.response.ApiResponse;
import com.example.chat.ops.contract.rbac.OpsAction;
import com.example.chat.ops.contract.rbac.OpsAuthorization;
import com.example.chat.ops.governance.service.application.GovernanceService;
import com.example.chat.ops.governance.service.domain.AuditEntry;
import com.example.chat.ops.governance.service.domain.PolicyProposal;
import com.example.chat.ops.governance.service.presentation.dto.CreatePolicyProposalRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class GovernanceController {
    private final GovernanceService governanceService;

    public GovernanceController(GovernanceService governanceService) {
        this.governanceService = governanceService;
    }

    @PostMapping("/policies/proposals")
    public ResponseEntity<ApiResponse<PolicyProposal>> createProposal(
            @RequestHeader("X-Project-Role") String role,
            @Valid @RequestBody CreatePolicyProposalRequest request
    ) {
        OpsAuthorization.require(role, OpsAction.POLICY_PROPOSAL_CREATE);
        PolicyProposal proposal = governanceService.createProposal(role, request);
        return ApiResponse.created(proposal).toResponseEntity();
    }

    @GetMapping("/audits")
    public ResponseEntity<ApiResponse<List<AuditEntry>>> listAudits(
            @RequestHeader("X-Project-Role") String role
    ) {
        OpsAuthorization.require(role, OpsAction.AUDIT_READ);
        return ApiResponse.ok(governanceService.audits()).toResponseEntity();
    }
}
