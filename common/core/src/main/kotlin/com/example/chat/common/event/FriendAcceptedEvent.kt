package com.example.chat.common.event

import java.time.Instant

/**
 * 친구 수락 이벤트
 */
data class FriendAcceptedEvent(
	val userId: String,
	val friendId: String,
	val occurredAt: Instant
)
