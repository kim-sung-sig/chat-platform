package com.example.chat.auth.server.infrastructure.persistence

import java.util.*
import org.springframework.data.jpa.repository.JpaRepository

interface JpaRefreshTokenRepository : JpaRepository<RefreshTokenEntity, UUID> {
    fun findByTokenValue(tokenValue: String): Optional<RefreshTokenEntity>

    fun deleteByTokenValue(tokenValue: String)

    fun deleteByPrincipalId(principalId: UUID)
}
