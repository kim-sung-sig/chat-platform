package com.example.chat.domain.user

/**
 * 사용자 Repository 인터페이스 (포트)
 */
interface UserRepository {

    /**
     * 사용자 저장
     */
    fun save(user: User): User

    /**
     * ID로 사용자 조회
     */
    fun findById(id: UserId): User?

    /**
     * 사용자 존재 여부 확인
     */
    fun existsById(id: UserId): Boolean

    /**
     * Username으로 사용자 조회
     */
    fun findByUsername(username: String): User?

    /**
     * Email로 사용자 조회
     */
    fun findByEmail(email: String): User?
}
