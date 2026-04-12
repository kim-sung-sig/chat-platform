package com.example.chat.ops.plan.service.application;

import com.example.chat.ops.contract.error.OpsErrorCode;
import com.example.chat.ops.contract.error.OpsException;
import com.example.chat.ops.contract.plan.PlanStatus;
import com.example.chat.ops.plan.service.domain.PlanDraft;
import com.example.chat.ops.plan.service.presentation.dto.CreatePlanRequest;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class PlanService {
    private final Map<String, PlanDraft> plans = new ConcurrentHashMap<>();

    public PlanDraft create(CreatePlanRequest request) {
        String planId = UUID.randomUUID().toString();
        PlanDraft plan = new PlanDraft(
                planId,
                request.projectId(),
                request.ticketRef(),
                request.objective(),
                PlanStatus.DRAFT,
                Instant.now()
        );
        plans.put(planId, plan);
        return plan;
    }

    public PlanDraft get(String planId) {
        PlanDraft plan = plans.get(planId);
        if (plan == null) {
            throw new OpsException(OpsErrorCode.PLAN_NOT_FOUND);
        }
        return plan;
    }
}
