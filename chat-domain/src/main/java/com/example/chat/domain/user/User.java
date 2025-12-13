package com.example.chat.domain.user;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * 사용자 Aggregate Root
 */
@Getter
@Builder
public class User {
	/**
	 * 시스템 사용자 ID (시스템 메시지용)
	 */
	public static final UserId SYSTEM_USER_ID = UserId.of("system");

	private final UserId id;
	private final Instant createdAt;
	private String username;
	private String email;
	private UserStatus status;
	private Instant updatedAt;
	private Instant lastActiveAt;

	/**
	 * 새로운 사용자 생성
	 */
	public static User create(String username, String email) {
		return User.builder()
				.id(UserId.generate())
				.username(username)
				.email(email)
				.status(UserStatus.ACTIVE)
				.createdAt(Instant.now())
				.updatedAt(Instant.now())
				.build();
	}

	/**
	 * 메시지 발송 가능 여부 확인
	 */
	public boolean canSendMessage() {
		return this.status == UserStatus.ACTIVE;
	}

	/**
	 * 사용자 정지
	 */
	public void suspend() {
		if (this.status == UserStatus.SUSPENDED) {
			throw new IllegalStateException("User is already suspended");
		}
		this.status = UserStatus.SUSPENDED;
		this.updatedAt = Instant.now();
	}

	/**
	 * 사용자 차단
	 */
	public void ban() {
		if (this.status == UserStatus.BANNED) {
			throw new IllegalStateException("User is already banned");
		}
		this.status = UserStatus.BANNED;
		this.updatedAt = Instant.now();
	}

	/**
	 * 사용자 활성화
	 */
	public void activate() {
		this.status = UserStatus.ACTIVE;
		this.updatedAt = Instant.now();
	}

	/**
	 * 마지막 활동 시간 업데이트
	 */
	public void updateLastActive() {
		this.lastActiveAt = Instant.now();
	}

	/**
	 * 차단되었는지 확인
	 */
	public boolean isBanned() {
		return this.status == UserStatus.BANNED;
	}

	/**
	 * 정지되었는지 확인
	 */
	public boolean isSuspended() {
		return this.status == UserStatus.SUSPENDED;
	}
}
