package com.example.chat.common.event

import java.time.Instant

/**
 * 친구 차단 이벤트
 */
data class FriendBlockedEvent(
	val userId: String,
	val blockedId: String,
	val occurredAt: Instant
)
