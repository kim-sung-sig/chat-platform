package com.example.chat.system.application.dto.response

import com.example.chat.domain.friendship.Friendship
import com.example.chat.domain.friendship.FriendshipStatus
import java.time.Instant

/**
 * 친구 관계 Response DTO
 */
data class FriendshipResponse(
	val id: String,
	val userId: String,
	val friendId: String,
	val status: FriendshipStatus,
	val nickname: String?,
	val favorite: Boolean,
	val createdAt: Instant,
	val updatedAt: Instant
) {
	companion object {
		fun from(friendship: Friendship) = FriendshipResponse(
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
}

/**
 * Extension Function for more idiomatic Kotlin
 */
fun Friendship.toResponse() = FriendshipResponse.from(this)
