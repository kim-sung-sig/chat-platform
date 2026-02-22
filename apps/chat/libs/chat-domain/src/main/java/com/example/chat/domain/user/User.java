package com.example.chat.domain.user;

import java.time.Instant;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 Aggregate Root
 *
 * 책임:
 * - 사용자 생명주기 관리
 * - 사용자 상태 관리
 * - 비즈니스 규칙 검증
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public class User {
    private UserId id;
    private String username;
    private String email;
    private String password;
    private UserStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant lastActiveAt;

    /**
     * 메시지 발송 가능 여부 확인
     */
    public boolean canSendMessage() {
        return status == UserStatus.ACTIVE;
    }

    /**
     * 사용자 정지
     */
    public void suspend() {
        if (status == UserStatus.SUSPENDED) {
            throw new IllegalStateException("User is already suspended");
        }
        status = UserStatus.SUSPENDED;
        updatedAt = Instant.now();
    }

    /**
     * 사용자 차단
     */
    public void ban() {
        if (status == UserStatus.BANNED) {
            throw new IllegalStateException("User is already banned");
        }
        status = UserStatus.BANNED;
        updatedAt = Instant.now();
    }

    /**
     * 사용자 활성화
     */
    public void activate() {
        status = UserStatus.ACTIVE;
        updatedAt = Instant.now();
    }

    /**
     * 마지막 활동 시간 업데이트
     */
    public void updateLastActive() {
        lastActiveAt = Instant.now();
    }

    /**
     * 차단되었는지 확인
     */
    public boolean isBanned() {
        return status == UserStatus.BANNED;
    }

    /**
     * 정지되었는지 확인
     */
    public boolean isSuspended() {
        return status == UserStatus.SUSPENDED;
    }

    /**
     * 시스템 사용자 ID (시스템 메시지용)
     */
    public static final UserId SYSTEM_USER_ID = UserId.of("system");

    /**
     * 새로운 사용자 생성
     */
    public static User create(String username, String email, String password) {
        Instant now = Instant.now();
        return User.builder()
                .id(UserId.generate())
                .username(username)
                .email(email)
                .password(password)
                .status(UserStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    /**
     * Storage Layer에서 재구성
     */
    public static User fromStorage(
            UserId id,
            String username,
            String email,
            String password,
            UserStatus status,
            Instant createdAt,
            Instant updatedAt,
            Instant lastActiveAt) {
        return User.builder()
                .id(id)
                .username(username)
                .email(email)
                .password(password)
                .status(status)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .lastActiveAt(lastActiveAt)
                .build();
    }
}
