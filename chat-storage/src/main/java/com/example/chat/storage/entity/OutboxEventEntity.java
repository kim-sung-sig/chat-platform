package com.example.chat.storage.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "ms_outbox_event", indexes = {
        @Index(name = "idx_outbox_state_created", columnList = "processed, created_at")
})
public class OutboxEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ms_outbox_event_id_gen")
    @SequenceGenerator(name = "ms_outbox_event_id_gen", sequenceName = "ms_outbox_event_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "aggregate_id", nullable = false, length = 128)
    private String aggregateId; // e.g., channelId or messageId

    @Column(name = "event_type", nullable = false, length = 64)
    private String eventType;

    @Lob
    @Column(name = "payload", nullable = false)
    private String payload; // JSON payload

    @Column(name = "processed", nullable = false)
    private boolean processed = false;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) this.createdAt = OffsetDateTime.now();
    }
}