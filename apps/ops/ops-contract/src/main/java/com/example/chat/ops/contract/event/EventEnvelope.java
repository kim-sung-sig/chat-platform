package com.example.chat.ops.contract.event;

import java.time.Instant;
import java.util.UUID;

public record EventEnvelope<T>(
        String eventId,
        String traceId,
        String projectId,
        Instant occurredAt,
        String eventType,
        T payload
) {
    public static <T> EventEnvelope<T> of(String traceId, String projectId, String eventType, T payload) {
        return new EventEnvelope<>(
                UUID.randomUUID().toString(),
                traceId,
                projectId,
                Instant.now(),
                eventType,
                payload
        );
    }
}
