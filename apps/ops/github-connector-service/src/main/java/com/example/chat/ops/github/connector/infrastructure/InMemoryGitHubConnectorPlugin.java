package com.example.chat.ops.github.connector.infrastructure;

import com.example.chat.ops.contract.ticket.CreateTicketCommand;
import com.example.chat.ops.contract.ticket.TicketAssignmentCommand;
import com.example.chat.ops.contract.ticket.TicketCommentCommand;
import com.example.chat.ops.contract.ticket.TicketConnector;
import com.example.chat.ops.contract.ticket.TicketRef;
import com.example.chat.ops.contract.ticket.TicketSnapshot;
import com.example.chat.ops.contract.ticket.TicketSource;
import com.example.chat.ops.contract.ticket.TicketStatusTransitionCommand;
import com.example.chat.ops.contract.ticket.UpdateTicketCommand;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

@Component
public class InMemoryGitHubConnectorPlugin implements TicketConnector {
    private final AtomicLong sequence = new AtomicLong(0);
    private final ConcurrentMap<String, TicketSnapshot> store = new ConcurrentHashMap<>();

    @Override
    public TicketSource source() {
        return TicketSource.GITHUB;
    }

    @Override
    public TicketRef createTicket(CreateTicketCommand command) {
        String externalId = Long.toString(sequence.incrementAndGet());
        TicketRef ref = new TicketRef(TicketSource.GITHUB, externalId, command.projectId());
        store.put(key(externalId, command.projectId()), new TicketSnapshot(
                ref,
                command.title(),
                command.body(),
                "OPEN",
                command.assignee()
        ));
        return ref;
    }

    @Override
    public void updateTicket(TicketRef reference, UpdateTicketCommand command) {
        TicketSnapshot current = store.get(key(reference.externalId(), reference.projectId()));
        if (current == null) {
            return;
        }

        store.put(key(reference.externalId(), reference.projectId()),
                new TicketSnapshot(reference, command.title(), command.body(), command.status(), current.assignee()));
    }

    @Override
    public void assign(TicketRef reference, TicketAssignmentCommand command) {
        TicketSnapshot current = store.get(key(reference.externalId(), reference.projectId()));
        if (current == null) {
            return;
        }

        store.put(key(reference.externalId(), reference.projectId()),
                new TicketSnapshot(reference, current.title(), current.body(), current.status(), command.assignee()));
    }

    @Override
    public void comment(TicketRef reference, TicketCommentCommand command) {
        // Stub for v1 foundation.
    }

    @Override
    public void transition(TicketRef reference, TicketStatusTransitionCommand command) {
        TicketSnapshot current = store.get(key(reference.externalId(), reference.projectId()));
        if (current == null) {
            return;
        }

        store.put(key(reference.externalId(), reference.projectId()),
                new TicketSnapshot(reference, current.title(), current.body(), command.toStatus(), current.assignee()));
    }

    @Override
    public Optional<TicketSnapshot> findByExternalId(String externalId, String projectId) {
        return Optional.ofNullable(store.get(key(externalId, projectId)));
    }

    private String key(String externalId, String projectId) {
        return projectId + ":" + externalId;
    }
}
