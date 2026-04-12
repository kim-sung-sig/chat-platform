package com.example.chat.ops.contract.ticket;

public record TicketStatusTransitionCommand(String fromStatus, String toStatus) {
}
