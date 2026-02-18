package com.example.chat.domain.friendship

import java.util.*

/**
 * 친구 관계 ID (Value Object)
 */
@JvmInline
value class FriendshipId(val value: String) {
	init {
		require(value.isNotBlank()) { "FriendshipId cannot be blank" }
	}

	companion object {
		/**
		 * 기존 ID로부터 생성
		 */
		fun of(value: String) = FriendshipId(value)

		/**
		 * 새로운 ID 생성
		 */
		fun generate() = FriendshipId(UUID.randomUUID().toString())
	}

	override fun toString(): String = value
}
