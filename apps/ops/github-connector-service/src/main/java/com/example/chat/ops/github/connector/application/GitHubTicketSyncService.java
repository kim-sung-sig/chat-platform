package com.example.chat.ops.github.connector.application;

import com.example.chat.ops.contract.event.EventEnvelope;
import com.example.chat.ops.contract.ticket.CreateTicketCommand;
import com.example.chat.ops.contract.ticket.TicketRef;
import com.example.chat.ops.github.connector.infrastructure.InMemoryGitHubConnectorPlugin;
import com.example.chat.ops.github.connector.presentation.dto.GitHubTicketSyncRequest;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class GitHubTicketSyncService {
    private final InMemoryGitHubConnectorPlugin connector;

    public GitHubTicketSyncService(InMemoryGitHubConnectorPlugin connector) {
        this.connector = connector;
    }

    public EventEnvelope<TicketRef> sync(GitHubTicketSyncRequest request) {
        TicketRef ticketRef = connector.createTicket(new CreateTicketCommand(
                request.projectId(),
                request.title(),
                request.body(),
                request.assignee()
        ));

        return EventEnvelope.of(
                UUID.randomUUID().toString(),
                request.projectId(),
                "ticket.synced",
                ticketRef
        );
    }
}
