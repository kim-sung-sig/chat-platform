package com.example.chat.ops.contract.ticket;

public record TicketSnapshot(
        TicketRef reference,
        String title,
        String body,
        String status,
        String assignee
) {
}
