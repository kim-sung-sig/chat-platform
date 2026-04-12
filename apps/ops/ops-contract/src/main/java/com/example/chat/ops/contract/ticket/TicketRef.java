package com.example.chat.ops.contract.ticket;

public record TicketRef(
        TicketSource source,
        String externalId,
        String projectId
) {
}
