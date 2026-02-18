package com.example.chat.domain.message

import java.util.UUID

/**
 * 메시지 ID (Value Object)
 */
data class MessageId(
    val value: String
) {
    init {
        require(value.isNotBlank()) { "MessageId cannot be null or blank" }
    }

    companion object {
        fun of(value: String): MessageId = MessageId(value)

        fun generate(): MessageId = MessageId(UUID.randomUUID().toString())
    }
}

