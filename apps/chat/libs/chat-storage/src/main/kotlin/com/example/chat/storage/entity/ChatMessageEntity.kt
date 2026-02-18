package com.example.chat.storage.entity

import com.example.chat.domain.message.MessageStatus
import com.example.chat.domain.message.MessageType
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
    name = "chat_messages",
    indexes = [
        Index(name = "idx_chat_message_channel_created", columnList = "channel_id, created_at"),
        Index(name = "idx_chat_message_sender", columnList = "sender_id")
    ]
)
class ChatMessageEntity(
    @Id
    @Column(name = "id", length = 36, nullable = false)
    var id: String,

    @Column(name = "channel_id", nullable = false, length = 36)
    var channelId: String,

    @Column(name = "sender_id", nullable = false, length = 36)
    var senderId: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 20)
    var messageType: MessageType,

    @Enumerated(EnumType.STRING)
    @Column(name = "message_status", nullable = false, length = 20)
    var messageStatus: MessageStatus = MessageStatus.PENDING,

    @Column(name = "content_text", length = 5000)
    var contentText: String? = null,

    @Column(name = "content_media_url", length = 500)
    var contentMediaUrl: String? = null,

    @Column(name = "content_file_name", length = 255)
    var contentFileName: String? = null,

    @Column(name = "content_file_size")
    var contentFileSize: Long? = null,

    @Column(name = "content_mime_type", length = 100)
    var contentMimeType: String? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),

    @Column(name = "sent_at")
    var sentAt: Instant? = null,

    @Column(name = "delivered_at")
    var deliveredAt: Instant? = null,

    @Column(name = "read_at")
    var readAt: Instant? = null
) {
    @PrePersist
    fun prePersist() {
        if (createdAt == Instant.EPOCH) {
            createdAt = Instant.now()
        }
    }

    protected constructor() : this(
        id = "",
        channelId = "",
        senderId = "",
        messageType = MessageType.TEXT
    )
}

