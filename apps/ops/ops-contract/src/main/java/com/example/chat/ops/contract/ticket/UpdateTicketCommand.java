package com.example.chat.ops.contract.ticket;

public record UpdateTicketCommand(
        String title,
        String body,
        String status
) {
}
