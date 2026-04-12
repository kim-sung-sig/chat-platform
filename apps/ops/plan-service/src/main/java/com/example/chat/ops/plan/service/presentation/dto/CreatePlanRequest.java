package com.example.chat.ops.plan.service.presentation.dto;

import com.example.chat.ops.contract.ticket.TicketRef;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreatePlanRequest(
        @NotBlank String projectId,
        @NotNull @Valid TicketRef ticketRef,
        @NotBlank String objective
) {
}
