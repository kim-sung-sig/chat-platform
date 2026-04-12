package com.example.chat.ops.approval.service.application;

import com.example.chat.ops.approval.service.domain.ApprovalRecord;
import com.example.chat.ops.contract.approval.ApprovalDecision;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class ApprovalService {
    private final Map<String, ApprovalRecord> decisions = new ConcurrentHashMap<>();

    public ApprovalRecord decide(String planId, ApprovalDecision decision, String decidedBy, String reason) {
        ApprovalRecord record = new ApprovalRecord(planId, decision, decidedBy, Instant.now(), reason);
        decisions.put(planId, record);
        return record;
    }

    public ApprovalRecord getByPlanId(String planId) {
        return decisions.get(planId);
    }
}
