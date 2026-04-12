package com.example.chat.ops.plugin.registry.presentation.dto;

import com.example.chat.ops.contract.ticket.TicketSource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;

public record RegisterPluginRequest(
        @NotBlank String id,
        @NotBlank String name,
        @NotBlank String version,
        @NotEmpty Set<TicketSource> supportedSources
) {
}
