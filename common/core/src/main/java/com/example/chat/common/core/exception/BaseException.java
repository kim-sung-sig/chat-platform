package com.example.chat.common.core.exception;

import lombok.Getter;

/**
 * 기본 공통 예외
 */
@Getter
public class BaseException extends RuntimeException {
    private final ErrorCode errorCode;
    private final Object[] args;

    public BaseException(ErrorCode errorCode) {
        this(errorCode, null, null);
    }

    public BaseException(ErrorCode errorCode, Object[] args) {
        this(errorCode, args, null);
    }

    public BaseException(ErrorCode errorCode, Object[] args, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.args = args;
    }
}
