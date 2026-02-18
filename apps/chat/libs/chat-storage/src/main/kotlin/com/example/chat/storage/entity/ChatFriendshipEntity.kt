package com.example.chat.storage.entity

import com.example.chat.domain.friendship.FriendshipStatus
import jakarta.persistence.*
import java.time.Instant

/**
 * 친구 관계 JPA Entity
 */
@Entity
@Table(
	name = "chat_friendships",
	indexes = [
		Index(name = "idx_user_id", columnList = "user_id"),
		Index(name = "idx_friend_id", columnList = "friend_id"),
		Index(name = "idx_user_status", columnList = "user_id, status")
	],
	uniqueConstraints = [
		UniqueConstraint(name = "uk_friendship", columnNames = ["user_id", "friend_id"])
	]
)
data class ChatFriendshipEntity(
	@Id
	@Column(name = "id", length = 36, nullable = false)
	val id: String,

	@Column(name = "user_id", length = 36, nullable = false)
	val userId: String,

	@Column(name = "friend_id", length = 36, nullable = false)
	val friendId: String,

	@Enumerated(EnumType.STRING)
	@Column(name = "status", length = 20, nullable = false)
	var status: FriendshipStatus,

	@Column(name = "nickname", length = 100)
	var nickname: String? = null,

	@Column(name = "favorite", nullable = false)
	var favorite: Boolean = false,

	@Column(name = "created_at", nullable = false)
	val createdAt: Instant,

	@Column(name = "updated_at", nullable = false)
	var updatedAt: Instant
)
