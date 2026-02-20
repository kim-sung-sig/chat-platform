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
        @JvmField
        val SYSTEM_USER_ID: UserId = UserId.of("system")

        /** Java-friendly builder() */
        @JvmStatic
        fun builder(): Builder = Builder()

        /** Java builder for User */
        class Builder {
            private var id: UserId? = null
            private var username: String? = null
            private var email: String? = null
            private var password: String? = null
            private var status: UserStatus? = null
            private var createdAt: Instant? = null
            private var updatedAt: Instant? = null
            private var lastActiveAt: Instant? = null

            fun id(id: UserId) = apply { this.id = id }
            fun username(username: String) = apply { this.username = username }
            fun email(email: String) = apply { this.email = email }
            fun password(password: String) = apply { this.password = password }
            fun status(status: UserStatus) = apply { this.status = status }
            fun createdAt(createdAt: Instant) = apply { this.createdAt = createdAt }
            fun updatedAt(updatedAt: Instant) = apply { this.updatedAt = updatedAt }
            fun lastActiveAt(lastActiveAt: Instant?) = apply { this.lastActiveAt = lastActiveAt }

            fun build(): User {
                val id = this.id ?: UserId.generate()
                val username = this.username ?: throw IllegalStateException("username is required")
                val email = this.email ?: throw IllegalStateException("email is required")
                val password = this.password ?: ""
                val status = this.status ?: UserStatus.ACTIVE
                val createdAt = this.createdAt ?: Instant.now()
                val updatedAt = this.updatedAt ?: Instant.now()

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
