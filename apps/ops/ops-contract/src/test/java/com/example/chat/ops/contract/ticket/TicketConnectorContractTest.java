package com.example.chat.ops.contract.ticket;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TicketConnectorContractTest {

    private final TicketConnector connector = new InMemoryConnector();

    @Test
    @DisplayName("connector creates and finds ticket by external id")
    void createAndFind() {
        TicketRef ref = connector.createTicket(new CreateTicketCommand("project-1", "Title", "Body", "alice"));

        Optional<TicketSnapshot> found = connector.findByExternalId(ref.externalId(), ref.projectId());

        assertThat(found).isPresent();
        assertThat(found.get().status()).isEqualTo("OPEN");
    }

    @Test
    @DisplayName("connector updates assigned user and status transition")
    void assignAndTransition() {
        TicketRef ref = connector.createTicket(new CreateTicketCommand("project-1", "Title", "Body", null));
        connector.assign(ref, new TicketAssignmentCommand("bob"));
        connector.transition(ref, new TicketStatusTransitionCommand("OPEN", "IN_PROGRESS"));

        TicketSnapshot snapshot = connector.findByExternalId(ref.externalId(), ref.projectId()).orElseThrow();

        assertThat(snapshot.assignee()).isEqualTo("bob");
        assertThat(snapshot.status()).isEqualTo("IN_PROGRESS");
    }

    private static final class InMemoryConnector implements TicketConnector {
        private final Map<String, TicketSnapshot> store = new ConcurrentHashMap<>();

        @Override
        public TicketSource source() {
            return TicketSource.GITHUB;
        }

        @Override
        public TicketRef createTicket(CreateTicketCommand command) {
            String id = Integer.toString(store.size() + 1);
            TicketRef ref = new TicketRef(TicketSource.GITHUB, id, command.projectId());
            store.put(key(ref.externalId(), ref.projectId()),
                    new TicketSnapshot(ref, command.title(), command.body(), "OPEN", command.assignee()));
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
            // no-op for contract test
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
}
