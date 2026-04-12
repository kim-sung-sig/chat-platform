package com.example.chat.ops.contract.plugin;

import com.example.chat.ops.contract.ticket.TicketSource;
import java.time.Instant;
import java.util.Set;

public record PluginDescriptor(
        String id,
        String name,
        String version,
        Set<TicketSource> supportedSources,
        Instant registeredAt
) {
}
