package com.example.chat.ops.contract.ticket;

public record CreateTicketCommand(
        String projectId,
        String title,
        String body,
        String assignee
) {
}
