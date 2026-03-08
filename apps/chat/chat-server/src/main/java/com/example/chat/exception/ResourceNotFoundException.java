package com.example.chat.exception;

import com.example.chat.common.core.exception.BaseException;
import com.example.chat.common.core.exception.ChatErrorCode;

/**
 * 리소스를 찾을 수 없을 때 발생하는 예외 (BaseException 상속)
 *
 * 사용: throw new ResourceNotFoundException(ChatErrorCode.CHANNEL_NOT_FOUND);
 */
public class ResourceNotFoundException extends BaseException {

    /** 동적 메시지 (레거시 호환) */
    public ResourceNotFoundException(String message) {
        super(ChatErrorCode.CHANNEL_NOT_FOUND, new Object[]{message});
    }

    /** 에러코드 직접 지정 (권장) */
    public ResourceNotFoundException(ChatErrorCode errorCode) {
        super(errorCode);
    }
}
