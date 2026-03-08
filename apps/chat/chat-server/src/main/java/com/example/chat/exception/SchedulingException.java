package com.example.chat.exception;

import com.example.chat.common.core.exception.BaseException;
import com.example.chat.common.core.exception.BaseErrorCode;

/**
 * 스케줄링 관련 예외 (BaseException 상속)
 */
public class SchedulingException extends BaseException {

    public SchedulingException(String message) {
        super(BaseErrorCode.INTERNAL_SERVER_ERROR, new Object[]{message});
    }

    public SchedulingException(String message, Throwable cause) {
        super(BaseErrorCode.INTERNAL_SERVER_ERROR, new Object[]{message}, cause);
    }
}
