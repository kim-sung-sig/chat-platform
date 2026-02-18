package com.example.chat.domain.channel

import java.util.UUID

/**
 * 채널 ID (Value Object)
 */
data class ChannelId(
    val value: String
) {
    init {
        require(value.isNotBlank()) { "ChannelId cannot be null or blank" }
    }

    companion object {
        fun of(value: String): ChannelId = ChannelId(value)

        fun generate(): ChannelId = ChannelId(UUID.randomUUID().toString())
    }
}

