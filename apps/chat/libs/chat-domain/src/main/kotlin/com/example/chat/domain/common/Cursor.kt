package com.example.chat.domain.common

/**
 * 커서 기반 페이징을 위한 Cursor (Value Object)
 */
data class Cursor(
    val value: String?
) {
    /**
     * 시작 커서인지 확인
     */
    fun isStart(): Boolean = value == null

    companion object {
        fun of(value: String?): Cursor? {
            return if (value.isNullOrBlank()) null else Cursor(value)
        }

        /**
         * 시작 커서 (첫 페이지)
         */
        fun start(): Cursor? = null  // null은 시작을 의미
    }
}

