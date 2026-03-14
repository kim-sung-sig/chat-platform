package com.example.chat.storage.domain.entity;

import java.time.Instant;

import com.example.chat.common.core.enums.FriendshipStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

/**
 * 친구 관계 JPA Entity
 */
@Entity
@Table(name = "chat_friendships", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_friend_id", columnList = "friend_id"),
        @Index(name = "idx_user_status", columnList = "user_id, status")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_friendship", columnNames = { "user_id", "friend_id" })
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ChatFriendshipEntity {
    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

    @Column(name = "friend_id", length = 36, nullable = false)
    private String friendId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private FriendshipStatus status;

    @Column(name = "nickname", length = 100)
    private String nickname;

    @Column(name = "favorite", nullable = false)
    @Builder.Default
    private boolean favorite = false;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }

    // =============================================
    // 비즈니스 메서드 - 상태 변경 캡슐화
    // =============================================

    public void accept() {
        if (this.status != FriendshipStatus.PENDING) {
            throw new IllegalStateException("PENDING 상태의 요청만 수락할 수 있습니다. 현재 상태: " + this.status);
        }
        this.status = FriendshipStatus.ACCEPTED;
    }


    public void block() {
        if (this.status == FriendshipStatus.BLOCKED) {
            throw new IllegalStateException("이미 차단된 관계입니다.");
        }
        this.status = FriendshipStatus.BLOCKED;
    }

    public void unblock() {
        if (this.status != FriendshipStatus.BLOCKED) {
            throw new IllegalStateException("차단된 관계만 차단 해제할 수 있습니다. 현재 상태: " + this.status);
        }
        this.status = FriendshipStatus.ACCEPTED;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void toggleFavorite() {
        this.favorite = !this.favorite;
    }
}
