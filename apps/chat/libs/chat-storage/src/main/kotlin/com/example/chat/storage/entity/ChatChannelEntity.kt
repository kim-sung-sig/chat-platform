package com.example.chat.storage.entity

import com.example.chat.domain.channel.ChannelType
import jakarta.persistence.*
import java.time.Instant

/**
 * 채널 JPA Entity
 */
@Entity
@Table(
    name = "chat_channels",
    indexes = [
        Index(name = "idx_chat_channel_owner", columnList = "owner_id"),
        Index(name = "idx_chat_channel_type", columnList = "channel_type")
    ]
)
class ChatChannelEntity(
    @Id
    @Column(name = "id", length = 36, nullable = false)
    var id: String,

    @Column(name = "name", nullable = false, length = 100)
    var name: String,

    @Column(name = "description", length = 500)
    var description: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "channel_type", nullable = false, length = 20)
    var channelType: ChannelType,

    @Column(name = "owner_id", nullable = false, length = 36)
    var ownerId: String,

    @Column(name = "active", nullable = false)
    var active: Boolean = true,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
) {
    @PrePersist
    fun prePersist() {
        if (createdAt == Instant.EPOCH) {
            createdAt = Instant.now()
        }
        if (updatedAt == Instant.EPOCH) {
            updatedAt = Instant.now()
        }
    }

    @PreUpdate
    fun preUpdate() {
        updatedAt = Instant.now()
    }

    // No-arg constructor for JPA
    protected constructor() : this(
        id = "",
        name = "",
        channelType = ChannelType.PRIVATE,
        ownerId = ""
    )
}

