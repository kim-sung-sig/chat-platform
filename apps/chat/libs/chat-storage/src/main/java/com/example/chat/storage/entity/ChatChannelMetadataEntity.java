package com.example.chat.storage.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 채팅방 메타데이터 JPA Entity
 */
@Entity
@Table(name = "chat_channel_metadata", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_channel_id", columnList = "channel_id"),
        @Index(name = "idx_user_activity", columnList = "user_id, last_activity_at DESC"),
        @Index(name = "idx_user_favorite", columnList = "user_id, favorite"),
        @Index(name = "idx_user_pinned", columnList = "user_id, pinned")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_channel_user", columnNames = { "channel_id", "user_id" })
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ChatChannelMetadataEntity {
    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "channel_id", length = 36, nullable = false)
    private String channelId;

    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

    @Column(name = "notification_enabled", nullable = false)
    @Builder.Default
    private boolean notificationEnabled = true;

    @Column(name = "favorite", nullable = false)
    @Builder.Default
    private boolean favorite = false;

    @Column(name = "pinned", nullable = false)
    @Builder.Default
    private boolean pinned = false;

    @Column(name = "last_read_message_id", length = 36)
    private String lastReadMessageId;

    @Column(name = "last_read_at")
    private Instant lastReadAt;

    @Column(name = "unread_count", nullable = false)
    @Builder.Default
    private int unreadCount = 0;

    @Column(name = "last_activity_at")
    private Instant lastActivityAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        if (createdAt == null)
            createdAt = now;
        if (updatedAt == null)
            updatedAt = now;
        if (lastActivityAt == null)
            lastActivityAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }
}
