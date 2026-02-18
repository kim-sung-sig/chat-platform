package com.example.chat.system.application.dto.response

import com.example.chat.domain.channel.metadata.ChannelMetadata
import java.time.Instant

/**
 * 채팅방 메타데이터 Response DTO
 */
data class ChannelMetadataResponse(
	val id: String,
	val channelId: String,
	val userId: String,
	val notificationEnabled: Boolean,
	val favorite: Boolean,
	val pinned: Boolean,
	val lastReadMessageId: String?,
	val lastReadAt: Instant?,
	val unreadCount: Int,
	val lastActivityAt: Instant,
	val createdAt: Instant,
	val updatedAt: Instant
) {
	companion object {
		fun from(metadata: ChannelMetadata) = ChannelMetadataResponse(
			id = metadata.id.value,
			channelId = metadata.channelId.value,
			userId = metadata.userId.value,
			notificationEnabled = metadata.notificationEnabled,
			favorite = metadata.favorite,
			pinned = metadata.pinned,
			lastReadMessageId = metadata.lastReadMessageId?.value,
			lastReadAt = metadata.lastReadAt,
			unreadCount = metadata.unreadCount,
			lastActivityAt = metadata.lastActivityAt,
			createdAt = metadata.createdAt,
			updatedAt = metadata.updatedAt
		)
	}
}

/**
 * Extension Function
 */
fun ChannelMetadata.toResponse() = ChannelMetadataResponse.from(this)
