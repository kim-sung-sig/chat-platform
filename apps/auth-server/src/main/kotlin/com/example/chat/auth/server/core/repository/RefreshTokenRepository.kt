package com.example.chat.auth.server.core.repository

import com.example.chat.auth.server.core.domain.RefreshToken
import java.util.*

interface RefreshTokenRepository {
    fun findByTokenValue(tokenValue: String): Optional<RefreshToken>

    fun save(refreshToken: RefreshToken)

    fun deleteByTokenValue(tokenValue: String)

    fun deleteByPrincipalId(principalId: UUID)
}
