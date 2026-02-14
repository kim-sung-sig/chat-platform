package com.example.chat.auth.server.infrastructure.persistence

import com.example.chat.auth.server.core.domain.RefreshToken
import com.example.chat.auth.server.core.repository.RefreshTokenRepository
import java.util.*
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class RefreshTokenRepositoryImpl(private val jpaRepository: JpaRefreshTokenRepository) :
        RefreshTokenRepository {
    override fun findByTokenValue(tokenValue: String): Optional<RefreshToken> {
        return jpaRepository.findByTokenValue(tokenValue).map { it.toDomain() }
    }

    @Transactional
    override fun save(refreshToken: RefreshToken) {
        jpaRepository.save(RefreshTokenEntity.fromDomain(refreshToken))
    }

    @Transactional
    override fun deleteByTokenValue(tokenValue: String) {
        jpaRepository.deleteByTokenValue(tokenValue)
    }

    @Transactional
    override fun deleteByPrincipalId(principalId: UUID) {
        jpaRepository.deleteByPrincipalId(principalId)
    }
}
