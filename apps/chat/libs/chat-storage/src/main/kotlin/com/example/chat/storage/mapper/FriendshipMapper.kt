package com.example.chat.storage.mapper

import com.example.chat.domain.friendship.Friendship
import com.example.chat.domain.friendship.FriendshipId
import com.example.chat.domain.user.UserId
import com.example.chat.storage.entity.ChatFriendshipEntity
import org.springframework.stereotype.Component

/**
 * Friendship Domain ↔ ChatFriendshipEntity 변환
 */
@Component
class FriendshipMapper {

	/**
	 * Domain → Entity 변환
	 */
	fun toEntity(friendship: Friendship): ChatFriendshipEntity {
		return ChatFriendshipEntity(
			id = friendship.id.value,
			userId = friendship.userId.value,
			friendId = friendship.friendId.value,
			status = friendship.status,
			nickname = friendship.nickname,
			favorite = friendship.favorite,
			createdAt = friendship.createdAt,
			updatedAt = friendship.updatedAt
		)
	}

	/**
	 * Entity → Domain 변환
	 */
	fun toDomain(entity: ChatFriendshipEntity): Friendship {
		return Friendship(
			id = FriendshipId.of(entity.id),
			userId = UserId.of(entity.userId),
			friendId = UserId.of(entity.friendId),
			status = entity.status,
			nickname = entity.nickname,
			favorite = entity.favorite,
			createdAt = entity.createdAt,
			updatedAt = entity.updatedAt
		)
	}
}

/**
 * Extension Functions for more idiomatic Kotlin
 */
fun Friendship.toEntity(): ChatFriendshipEntity {
	return ChatFriendshipEntity(
		id = id.value,
		userId = userId.value,
		friendId = friendId.value,
		status = status,
		nickname = nickname,
		favorite = favorite,
		createdAt = createdAt,
		updatedAt = updatedAt
	)
}

fun ChatFriendshipEntity.toDomain(): Friendship {
	return Friendship(
		id = FriendshipId.of(id),
		userId = UserId.of(userId),
		friendId = UserId.of(friendId),
		status = status,
		nickname = nickname,
		favorite = favorite,
		createdAt = createdAt,
		updatedAt = updatedAt
	)
}
