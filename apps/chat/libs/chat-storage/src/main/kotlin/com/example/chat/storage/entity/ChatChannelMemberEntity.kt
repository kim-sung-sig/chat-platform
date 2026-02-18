package com.example.chat.storage.entity

import jakarta.persistence.*
import java.time.Instant

/**
 * 채널 멤버 JPA Entity
 */
@Entity
@Table(
    name = "chat_channel_members",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["channel_id", "user_id"])
    ],
    indexes = [
        Index(name = "idx_channel_member_channel", columnList = "channel_id"),
        Index(name = "idx_channel_member_user", columnList = "user_id")
    ]
)
class ChatChannelMemberEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "channel_id", nullable = false, length = 36)
    var channelId: String,

    @Column(name = "user_id", nullable = false, length = 36)
    var userId: String,

    @Column(name = "joined_at", nullable = false)
    var joinedAt: Instant = Instant.now()
) {
    @PrePersist
    fun prePersist() {
        if (joinedAt == Instant.EPOCH) {
            joinedAt = Instant.now()
        }
    }

    // No-arg constructor for JPA
    protected constructor() : this(
        channelId = "",
        userId = ""
    )
}

