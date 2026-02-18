package com.example.chat.domain.channel.metadata

import java.util.*

/**
 * 채팅방 메타데이터 ID (Value Object)
 */
@JvmInline
value class ChannelMetadataId(val value: String) {
	init {
		require(value.isNotBlank()) { "ChannelMetadataId cannot be blank" }
	}

	companion object {
		fun of(value: String) = ChannelMetadataId(value)
		fun generate() = ChannelMetadataId(UUID.randomUUID().toString())
	}

	override fun toString(): String = value
}
