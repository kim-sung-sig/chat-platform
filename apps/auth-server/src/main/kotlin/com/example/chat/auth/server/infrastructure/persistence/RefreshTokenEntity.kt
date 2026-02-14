package com.example.chat.auth.server.infrastructure.persistence

import com.example.chat.auth.server.core.domain.RefreshToken
import com.example.chat.auth.server.core.domain.credential.Device
import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity
@Table(name = "auth_refresh_tokens")
class RefreshTokenEntity(
        @Id val id: UUID,
        @Column(name = "principal_id", nullable = false) val principalId: UUID,
        @Embedded
        @AttributeOverrides(
                AttributeOverride(name = "deviceId", column = Column(name = "device_id")),
                AttributeOverride(name = "platform", column = Column(name = "platform")),
                AttributeOverride(name = "browser", column = Column(name = "browser"))
        )
        val device: Device,
        @Column(name = "token_value", length = 1024, nullable = false, unique = true)
        val tokenValue: String,
        @Column(name = "expiry_at", nullable = false) val expiryAt: Instant,
        @Column(name = "created_at", nullable = false) val createdAt: Instant,
        @Column(name = "last_used_at") val lastUsedAt: Instant?
) {
    fun toDomain(): RefreshToken {
        return RefreshToken(
                id = id,
                principalId = principalId,
                device = device,
                tokenValue = tokenValue,
                expiryAt = expiryAt,
                createdAt = createdAt,
                lastUsedAt = lastUsedAt ?: createdAt
        )
    }

    companion object {
        fun fromDomain(domain: RefreshToken): RefreshTokenEntity {
            return RefreshTokenEntity(
                    id = domain.id,
                    principalId = domain.principalId,
                    device = domain.device,
                    tokenValue = domain.tokenValue,
                    expiryAt = domain.expiryAt,
                    createdAt = domain.createdAt,
                    lastUsedAt = domain.lastUsedAt
            )
        }
    }
}
