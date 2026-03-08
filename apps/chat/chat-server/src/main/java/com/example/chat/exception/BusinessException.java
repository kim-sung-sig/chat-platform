package com.example.chat.exception;

import com.example.chat.common.core.exception.BaseException;
import com.example.chat.common.core.exception.ChatErrorCode;

/**
 * 비즈니스 로직 위반 시 발생하는 예외 (BaseException 상속)
 */
public class BusinessException extends BaseException {

    public BusinessException(String message) {
        super(ChatErrorCode.CHANNEL_NOT_ACTIVE, new Object[]{message});
    }

    public BusinessException(ChatErrorCode errorCode) {
        super(errorCode);
    }
}
