package com.example.chat.ops.plan.service.presentation;

import com.example.chat.common.web.response.ApiResponse;
import com.example.chat.ops.contract.rbac.OpsAction;
import com.example.chat.ops.contract.rbac.OpsAuthorization;
import com.example.chat.ops.plan.service.application.PlanService;
import com.example.chat.ops.plan.service.domain.PlanDraft;
import com.example.chat.ops.plan.service.presentation.dto.CreatePlanRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/plans")
public class PlanController {
    private final PlanService planService;

    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PlanDraft>> createPlan(
            @RequestHeader("X-Project-Role") String role,
            @Valid @RequestBody CreatePlanRequest request
    ) {
        OpsAuthorization.require(role, OpsAction.PLAN_CREATE);
        return ApiResponse.created(planService.create(request)).toResponseEntity();
    }

    @GetMapping("/{planId}")
    public ResponseEntity<ApiResponse<PlanDraft>> getPlan(
            @RequestHeader("X-Project-Role") String role,
            @PathVariable String planId
    ) {
        OpsAuthorization.require(role, OpsAction.AUDIT_READ);
        return ApiResponse.ok(planService.get(planId)).toResponseEntity();
    }
}
