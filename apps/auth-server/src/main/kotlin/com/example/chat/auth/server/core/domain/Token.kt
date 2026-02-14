package com.example.chat.auth.server.core.domain

import java.time.Instant
import java.util.UUID

/**
 * JWT 토큰 정보
 * - 인증 성공 후 발급되는 토큰
 * - AuthLevel과 Principal 정보를 담음
 */
data class Token(
        val accessToken: String,
        val refreshToken: String?,
        val expiresAt: Instant,
        val refreshExpiresAt: Instant?,
        val principalId: UUID,
        val authLevel: AuthLevel,
        val tokenType: TokenType
) {
    fun isExpired(): Boolean = Instant.now().isAfter(expiresAt)
}
