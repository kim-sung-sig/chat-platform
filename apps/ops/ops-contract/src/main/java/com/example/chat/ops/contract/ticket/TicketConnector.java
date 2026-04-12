package com.example.chat.ops.contract.ticket;

import java.util.Optional;

public interface TicketConnector {
    TicketSource source();

    TicketRef createTicket(CreateTicketCommand command);

    void updateTicket(TicketRef reference, UpdateTicketCommand command);

    void assign(TicketRef reference, TicketAssignmentCommand command);

    void comment(TicketRef reference, TicketCommentCommand command);

    void transition(TicketRef reference, TicketStatusTransitionCommand command);

    Optional<TicketSnapshot> findByExternalId(String externalId, String projectId);
}
