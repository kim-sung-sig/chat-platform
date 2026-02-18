package com.example.chat.domain.user

import java.time.Instant

/**
 * 사용자 Aggregate Root
 *
 * 책임:
 * - 사용자 생명주기 관리
 * - 사용자 상태 관리
 * - 비즈니스 규칙 검증
 */
class User private constructor(
    val id: UserId,
    private var _username: String,
    private var _email: String,
    private var _password: String,
    private var _status: UserStatus,
    val createdAt: Instant,
    private var _updatedAt: Instant,
    private var _lastActiveAt: Instant? = null
) {
    val username: String get() = _username
    val email: String get() = _email
    val password: String get() = _password
    val status: UserStatus get() = _status
    val updatedAt: Instant get() = _updatedAt
    val lastActiveAt: Instant? get() = _lastActiveAt

    /**
     * 메시지 발송 가능 여부 확인
     */
    fun canSendMessage(): Boolean = _status == UserStatus.ACTIVE

    /**
     * 사용자 정지
     */
    fun suspend() {
        check(_status != UserStatus.SUSPENDED) { "User is already suspended" }
        _status = UserStatus.SUSPENDED
        _updatedAt = Instant.now()
    }

    /**
     * 사용자 차단
     */
    fun ban() {
        check(_status != UserStatus.BANNED) { "User is already banned" }
        _status = UserStatus.BANNED
        _updatedAt = Instant.now()
    }

    /**
     * 사용자 활성화
     */
    fun activate() {
        _status = UserStatus.ACTIVE
        _updatedAt = Instant.now()
    }

    /**
     * 마지막 활동 시간 업데이트
     */
    fun updateLastActive() {
        _lastActiveAt = Instant.now()
    }

    /**
     * 차단되었는지 확인
     */
    fun isBanned(): Boolean = _status == UserStatus.BANNED

    /**
     * 정지되었는지 확인
     */
    fun isSuspended(): Boolean = _status == UserStatus.SUSPENDED

    companion object {
        /**
         * 시스템 사용자 ID (시스템 메시지용)
         */
        val SYSTEM_USER_ID: UserId = UserId.of("system")

        /**
         * 새로운 사용자 생성
         */
        fun create(username: String, email: String, password: String): User {
            return User(
                id = UserId.generate(),
                _username = username,
                _email = email,
                _password = password,
                _status = UserStatus.ACTIVE,
                createdAt = Instant.now(),
                _updatedAt = Instant.now()
            )
        }

        /**
         * Storage Layer에서 재구성
         * Entity로부터 도메인 모델 복원 시 사용
         */
        @JvmStatic
        fun fromStorage(
            id: UserId,
            username: String,
            email: String,
            password: String,
            status: UserStatus,
            createdAt: Instant,
            updatedAt: Instant,
            lastActiveAt: Instant?
        ): User {
            return User(
                id = id,
                _username = username,
                _email = email,
                _password = password,
                _status = status,
                createdAt = createdAt,
                _updatedAt = updatedAt,
                _lastActiveAt = lastActiveAt
            )
        }
    }
}

