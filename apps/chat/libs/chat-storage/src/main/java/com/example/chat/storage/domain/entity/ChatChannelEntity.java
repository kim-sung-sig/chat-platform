package com.example.chat.storage.domain.entity;

import java.time.Instant;

import com.example.chat.common.core.enums.ChannelType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 채널 JPA Entity
 */
@Entity
@Table(name = "chat_channels", indexes = {
        @Index(name = "idx_chat_channel_owner", columnList = "owner_id"),
        @Index(name = "idx_chat_channel_type", columnList = "channel_type")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ChatChannelEntity {
    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel_type", nullable = false, length = 20)
    private ChannelType channelType;

    @Column(name = "owner_id", nullable = false, length = 36)
    private String ownerId;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @PrePersist
    public void prePersist() {
        if (createdAt == null || createdAt.equals(Instant.EPOCH)) {
            createdAt = Instant.now();
        }
        if (updatedAt == null || updatedAt.equals(Instant.EPOCH)) {
            updatedAt = Instant.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }

    // =============================================
    // 비즈니스 메서드 - 상태 변경 캡슐화
    // =============================================

    public void deactivate() {
        if (!this.active) {
            throw new IllegalStateException("이미 비활성화된 채널입니다: " + this.id);
        }
        this.active = false;
    }

    public void activate() {
        if (this.active) {
            throw new IllegalStateException("이미 활성화된 채널입니다: " + this.id);
        }
        this.active = true;
    }

    public void updateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("채널 이름은 비어 있을 수 없습니다.");
        }
        this.name = name;
    }

    public void updateDescription(String description) {
        this.description = description;
    }
}
