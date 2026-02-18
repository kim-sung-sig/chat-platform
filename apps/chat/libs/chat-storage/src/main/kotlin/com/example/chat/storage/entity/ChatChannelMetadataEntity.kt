package com.example.chat.storage.entity

import jakarta.persistence.*
import java.time.Instant

/**
 * 채팅방 메타데이터 JPA Entity
 */
@Entity
@Table(
	name = "chat_channel_metadata",
	indexes = [
		Index(name = "idx_user_id", columnList = "user_id"),
		Index(name = "idx_channel_id", columnList = "channel_id"),
		Index(name = "idx_user_activity", columnList = "user_id, last_activity_at DESC"),
		Index(name = "idx_user_favorite", columnList = "user_id, favorite"),
		Index(name = "idx_user_pinned", columnList = "user_id, pinned")
	],
	uniqueConstraints = [
		UniqueConstraint(name = "uk_channel_user", columnNames = ["channel_id", "user_id"])
	]
)
data class ChatChannelMetadataEntity(
	@Id
	@Column(name = "id", length = 36, nullable = false)
	val id: String,

	@Column(name = "channel_id", length = 36, nullable = false)
	val channelId: String,

	@Column(name = "user_id", length = 36, nullable = false)
	val userId: String,

	@Column(name = "notification_enabled", nullable = false)
	var notificationEnabled: Boolean = true,

	@Column(name = "favorite", nullable = false)
	var favorite: Boolean = false,

	@Column(name = "pinned", nullable = false)
	var pinned: Boolean = false,

	@Column(name = "last_read_message_id", length = 36)
	var lastReadMessageId: String? = null,

	@Column(name = "last_read_at")
	var lastReadAt: Instant? = null,

	@Column(name = "unread_count", nullable = false)
	var unreadCount: Int = 0,

	@Column(name = "last_activity_at")
	var lastActivityAt: Instant? = null,

	@Column(name = "created_at", nullable = false)
	val createdAt: Instant,

	@Column(name = "updated_at", nullable = false)
	var updatedAt: Instant
)
