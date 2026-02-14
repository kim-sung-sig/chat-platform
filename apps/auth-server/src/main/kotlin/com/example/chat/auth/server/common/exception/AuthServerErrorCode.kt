package com.example.chat.auth.server.common.exception

import com.example.chat.common.core.exception.ErrorCode

enum class AuthServerErrorCode(
        override val code: String,
        override val message: String,
        override val status: Int
) : ErrorCode {

    // Authentication Errors
    INVALID_CREDENTIALS("AUTH-401-001", "아이디 또는 비밀번호가 일치하지 않습니다", 401),
    PRINCIPAL_NOT_FOUND("AUTH-401-002", "존재하지 않는 사용자입니다", 401),
    PRINCIPAL_INACTIVE("AUTH-401-003", "비활성화된 계정입니다", 401),

    // Token Errors
    TOKEN_EXPIRED("AUTH-401-010", "토큰이 만료되었습니다", 401),
    INVALID_TOKEN("AUTH-401-011", "유효하지 않은 토큰입니다", 401),
    TOKEN_SIGNATURE_INVALID("AUTH-401-012", "토큰 서명이 유효하지 않습니다", 401),
    REFRESH_TOKEN_NOT_FOUND("AUTH-401-013", "리프레시 토큰을 찾을 수 없습니다", 401),
    TOKEN_REVOKED("AUTH-401-014", "이미 무효화된 토큰입니다", 401),

    // MFA Errors
    MFA_REQUIRED("AUTH-401-030", "MFA 추가 인증이 필요합니다", 401),
    INVALID_MFA_CODE("AUTH-401-031", "MFA 인증 코드가 일치하지 않습니다", 401),
    MFA_SESSION_EXPIRED("AUTH-401-032", "MFA 세션이 만료되었습니다", 401),

    // OAuth Errors
    SOCIAL_AUTH_FAILED("AUTH-401-050", "소셜 인증에 실패했습니다", 401),

    // Authorization Errors
    INSUFFICIENT_AUTH_LEVEL("AUTH-403-001", "인증 수준이 권한을 수행하기에 부족합니다", 403),
    ACCESS_DENIED("AUTH-403-002", "접근 권한이 없습니다", 403),

    // Common Errors (Mapped to AUTH prefix for monitoring)
    BAD_REQUEST("AUTH-400-000", "잘못된 요청입니다", 400),
    INTERNAL_SERVER_ERROR("AUTH-500-000", "서버 내부 오류가 발생했습니다", 500)
}
