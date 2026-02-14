package com.example.chat.auth.jwt.error

import com.example.chat.common.core.exception.ErrorCode

enum class AuthErrorCode(
        override val code: String,
        override val message: String,
        override val status: Int
) : ErrorCode {
    INVALID_TOKEN("AUTH_001", "유효하지 않은 토큰", 401),
    EXPIRED_TOKEN("AUTH_002", "만료된 토큰", 401),
    INSUFFICIENT_SCOPE("AUTH_003", "권한이 부족합니다", 403)
}
