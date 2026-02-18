package com.example.chat.storage.entity

import com.example.chat.domain.user.UserStatus
import jakarta.persistence.*
import java.time.Instant

/**
 * 사용자 JPA Entity
 */
@Entity
@Table(
    name = "users",
    indexes = [
        Index(name = "idx_username", columnList = "username"),
        Index(name = "idx_email", columnList = "email"),
        Index(name = "idx_status", columnList = "status")
    ]
)
class UserEntity(
    @Id
    @Column(name = "id", length = 36, nullable = false)
    var id: String,

    @Column(name = "username", length = 50, nullable = false, unique = true)
    var username: String,

    @Column(name = "email", length = 255, nullable = false, unique = true)
    var email: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    var status: UserStatus = UserStatus.ACTIVE,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),

    @Column(name = "updated_at")
    var updatedAt: Instant? = null,

    @Column(name = "last_active_at")
    var lastActiveAt: Instant? = null
) {
    @PrePersist
    fun prePersist() {
        if (createdAt == Instant.EPOCH) {
            createdAt = Instant.now()
        }
    }

    @PreUpdate
    fun preUpdate() {
        updatedAt = Instant.now()
    }

    // No-arg constructor for JPA
    protected constructor() : this(
        id = "",
        username = "",
        email = ""
    )
}

