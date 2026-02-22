package com.example.chat.auth.server.common.exception;

import com.example.chat.common.core.exception.BaseException;
import com.example.chat.common.core.exception.ErrorCode;

public class AuthException extends BaseException {
    public AuthException(ErrorCode errorCode) {
        super(errorCode);
    }

    public AuthException(ErrorCode errorCode, Object[] args) {
        super(errorCode, args);
    }

    public AuthException(ErrorCode errorCode, Object[] args, Throwable cause) {
        super(errorCode, args, cause);
    }
}
