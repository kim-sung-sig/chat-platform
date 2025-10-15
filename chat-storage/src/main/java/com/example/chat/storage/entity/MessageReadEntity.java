package com.example.chat.storage.entity;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "ms_message_read",
       uniqueConstraints = @UniqueConstraint(name = "uq_user_message_read", columnNames = {"user_id", "message_id"}))
@NoArgsConstructor
public class MessageReadEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message_id", nullable = false)
    private Long messageId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "read_at", nullable = false)
    private OffsetDateTime readAt;

    public MessageReadEntity(Long messageId, Long userId) {
        this.messageId = messageId;
        this.userId = userId;
    }

    @PrePersist
    public void prePersist() {
        if (this.readAt == null) this.readAt = OffsetDateTime.now();
    }

}