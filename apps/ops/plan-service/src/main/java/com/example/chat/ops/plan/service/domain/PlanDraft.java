package com.example.chat.ops.plan.service.domain;

import com.example.chat.ops.contract.plan.PlanStatus;
import com.example.chat.ops.contract.ticket.TicketRef;
import java.time.Instant;

public record PlanDraft(
        String planId,
        String projectId,
        TicketRef ticketRef,
        String objective,
        PlanStatus status,
        Instant createdAt
) {
}
