package com.example.chat.auth.jwt.error;

import com.example.chat.common.core.exception.BaseException;

/**
 * 인증/인가 관련 예외의 기반 클래스
 * ErrorResponse 자동 변환을 위해 BaseException을 상속합니다.
 */
public abstract class AuthException extends BaseException {

    protected AuthException(AuthErrorCode errorCode) {
        super(errorCode);
    }
}
