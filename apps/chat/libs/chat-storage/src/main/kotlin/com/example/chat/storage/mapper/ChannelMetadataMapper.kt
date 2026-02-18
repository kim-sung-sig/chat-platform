package com.example.chat.storage.mapper

import com.example.chat.domain.channel.ChannelId
import com.example.chat.domain.channel.metadata.ChannelMetadata
import com.example.chat.domain.channel.metadata.ChannelMetadataId
import com.example.chat.domain.message.MessageId
import com.example.chat.domain.user.UserId
import com.example.chat.storage.entity.ChatChannelMetadataEntity
import org.springframework.stereotype.Component

/**
 * ChannelMetadata Domain ↔ ChatChannelMetadataEntity 변환
 */
@Component
class ChannelMetadataMapper {

	/**
	 * Domain → Entity 변환
	 */
	fun toEntity(metadata: ChannelMetadata): ChatChannelMetadataEntity {
		return ChatChannelMetadataEntity(
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

	/**
	 * Entity → Domain 변환
	 */
	fun toDomain(entity: ChatChannelMetadataEntity): ChannelMetadata {
		return ChannelMetadata(
			id = ChannelMetadataId.of(entity.id),
			channelId = ChannelId.of(entity.channelId),
			userId = UserId.of(entity.userId),
			notificationEnabled = entity.notificationEnabled,
			favorite = entity.favorite,
			pinned = entity.pinned,
			lastReadMessageId = entity.lastReadMessageId?.let { MessageId.of(it) },
			lastReadAt = entity.lastReadAt,
			unreadCount = entity.unreadCount,
			lastActivityAt = entity.lastActivityAt ?: entity.createdAt,
			createdAt = entity.createdAt,
			updatedAt = entity.updatedAt
		)
	}
}

/**
 * Extension Functions for more idiomatic Kotlin
 */
fun ChannelMetadata.toEntity(): ChatChannelMetadataEntity {
	return ChatChannelMetadataEntity(
		id = id.value,
		channelId = channelId.value,
		userId = userId.value,
		notificationEnabled = notificationEnabled,
		favorite = favorite,
		pinned = pinned,
		lastReadMessageId = lastReadMessageId?.value,
		lastReadAt = lastReadAt,
		unreadCount = unreadCount,
		lastActivityAt = lastActivityAt,
		createdAt = createdAt,
		updatedAt = updatedAt
	)
}

fun ChatChannelMetadataEntity.toDomain(): ChannelMetadata {
	return ChannelMetadata(
		id = ChannelMetadataId.of(id),
		channelId = ChannelId.of(channelId),
		userId = UserId.of(userId),
		notificationEnabled = notificationEnabled,
		favorite = favorite,
		pinned = pinned,
		lastReadMessageId = lastReadMessageId?.let { MessageId.of(it) },
		lastReadAt = lastReadAt,
		unreadCount = unreadCount,
		lastActivityAt = lastActivityAt ?: createdAt,
		createdAt = createdAt,
		updatedAt = updatedAt
	)
}
