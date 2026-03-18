package com.example.chat.storage.domain.entity;

import java.time.Instant;

import com.example.chat.common.core.enums.UserStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 JPA Entity.
 * createdAt / updatedAt 생명주기는 {@link BaseEntity} 에서 관리한다.
 */
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_username", columnList = "username"),
        @Index(name = "idx_user_email", columnList = "email"),
        @Index(name = "idx_user_status", columnList = "status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity extends BaseEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "username", length = 50, nullable = false, unique = true)
    private String username;

    @Column(name = "email", length = 255, nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "last_active_at")
    private Instant lastActiveAt;

    private UserEntity(String id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.status = UserStatus.ACTIVE;
    }

    /**
     * 새 사용자 엔티티를 생성하는 팩토리 메서드.
     */
    public static UserEntity create(String id, String username, String email) {
        return new UserEntity(id, username, email);
    }

    // =============================================
    // 비즈니스 메서드 - 상태 변경 캡슐화
    // =============================================

    public void activate() {
        if (this.status == UserStatus.ACTIVE) {
            throw new IllegalStateException("이미 활성 상태입니다.");
        }
        this.status = UserStatus.ACTIVE;
    }

    public void suspend() {
        if (this.status == UserStatus.SUSPENDED) {
            throw new IllegalStateException("이미 정지 상태입니다.");
        }
        this.status = UserStatus.SUSPENDED;
    }

    public void ban() {
        if (this.status == UserStatus.BANNED) {
            throw new IllegalStateException("이미 차단 상태입니다.");
        }
        this.status = UserStatus.BANNED;
    }

    public void updateLastActive() {
        this.lastActiveAt = Instant.now();
    }

    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }

    public boolean isSuspended() {
        return this.status == UserStatus.SUSPENDED;
    }

    public boolean isBanned() {
        return this.status == UserStatus.BANNED;
    }
}
