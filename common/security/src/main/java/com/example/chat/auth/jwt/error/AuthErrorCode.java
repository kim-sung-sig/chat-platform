package com.example.chat.auth.jwt.error;

import com.example.chat.common.core.exception.ErrorCode;

/**
 * 인증 에러 코드
 */
public enum AuthErrorCode implements ErrorCode {
    INVALID_TOKEN("AUTH_001", "유효하지 않은 토큰", 401),
    EXPIRED_TOKEN("AUTH_002", "만료된 토큰", 401),
    INSUFFICIENT_SCOPE("AUTH_003", "권한이 부족합니다", 403);

    private final String code;
    private final String message;
    private final int status;

    AuthErrorCode(String code, String message, int status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public int getStatus() {
        return status;
    }
}
