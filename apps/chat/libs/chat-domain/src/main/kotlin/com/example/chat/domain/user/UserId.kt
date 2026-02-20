package com.example.chat.domain.user

import java.util.UUID

/**
 * 사용자 ID (Value Object)
 */
data class UserId(
    val value: String
) {
    init {
        require(value.isNotBlank()) { "UserId cannot be null or blank" }
    }

    companion object {
        @JvmStatic
        fun of(value: String): UserId = UserId(value)

        @JvmStatic
        fun generate(): UserId = UserId(UUID.randomUUID().toString())
    }
}
