
package com.example.chat.auth.server.core.domain

import com.example.chat.auth.server.core.domain.credential.Device
import java.time.Duration
import java.time.Instant
import java.util.UUID

data class RefreshToken(
    val id: UUID,
    val principalId: UUID,
    val device: Device,
    var tokenValue: String,
    var expiryAt: Instant,
    val createdAt: Instant,
    var lastUsedAt: Instant? = null
) {
    fun isExpired(): Boolean = Instant.now().isAfter(expiryAt)

    fun isExpiringWithin(duration: Duration): Boolean =
        expiryAt.isBefore(Instant.now().plus(duration))

    fun updateToken(newTokenValue: String, newExpiryAt: Instant) {
        this.tokenValue = newTokenValue
        this.expiryAt = newExpiryAt
        this.lastUsedAt = Instant.now()
    }

    fun used() {
        this.lastUsedAt = Instant.now()
    }
}
