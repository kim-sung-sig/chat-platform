package com.example.chat.common.event

import java.time.Instant

/**
 * 친구 요청 이벤트
 */
data class FriendRequestedEvent(
	val requesterId: String,
	val targetId: String,
	val occurredAt: Instant
)
