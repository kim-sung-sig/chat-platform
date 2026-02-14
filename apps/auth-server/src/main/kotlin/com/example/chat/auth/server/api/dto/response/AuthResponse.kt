package com.example.chat.auth.server.api.dto.response

import com.example.chat.auth.server.core.domain.AuthResult
import com.example.chat.auth.server.core.domain.Token
import java.time.Duration
import java.time.Instant

data class AuthResponse(
        val isAuthenticated: Boolean,
        val authLevel: String? = null,
        val completedCredentials: Set<String>? = null,
        val requiresMfa: Boolean = false,
        val mfaSessionId: String? = null,
        val remainingMfaMethods: List<String>? = null,
        val failureReason: String? = null,
        val token: TokenResponse? = null
) {
        companion object {
                fun from(result: AuthResult, token: Token? = null): AuthResponse {
                        val expiresIn =
                                token?.let {
                                        Duration.between(Instant.now(), it.expiresAt).toSeconds()
                                }
                                        ?: 0L

                        return AuthResponse(
                                isAuthenticated = result.isAuthenticated,
                                authLevel = result.authLevel?.name,
                                completedCredentials =
                                        result.completedCredentials.map { it.name }.toSet(),
                                requiresMfa = result.requiresMfa(),
                                mfaSessionId = result.mfaRequirement?.sessionId,
                                remainingMfaMethods =
                                        result.mfaRequirement?.getRemainingMethods()?.map {
                                                it.name
                                        },
                                failureReason = result.failureReason,
                                token =
                                        token?.let {
                                                TokenResponse(
                                                        accessToken = it.accessToken,
                                                        refreshToken = it.refreshToken,
                                                        tokenType = it.tokenType.name,
                                                        expiresIn = expiresIn
                                                )
                                        }
                        )
                }
        }
}
