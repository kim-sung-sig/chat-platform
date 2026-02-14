package com.example.chat.auth.server.core.domain

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

/**
 * 인증 대상(주체) - JPA Entity
 * - User, ServiceAccount 등
 */
@Entity
@Table(name = "principals")
class Principal(
        @Id @Column(name = "id", columnDefinition = "uuid") val id: UUID,
        @Column(name = "identifier", unique = true, nullable = false) val identifier: String,
        @Enumerated(EnumType.STRING)
        @Column(name = "type", nullable = false)
        val type: PrincipalType,
        @Column(name = "active", nullable = false) var active: Boolean = true
) {
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = Instant.now()
        private set

    @Column(name = "updated_at")
    var updatedAt: Instant? = Instant.now()
        private set

    @PrePersist
    protected fun onCreate() {
        createdAt = Instant.now()
        updatedAt = Instant.now()
    }

    @PreUpdate
    protected fun onUpdate() {
        updatedAt = Instant.now()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Principal) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "Principal(id=$id, identifier='$identifier', type=$type, active=$active)"
    }
}
