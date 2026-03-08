package com.example.chat.exception;

import com.example.chat.common.core.exception.BaseException;
import com.example.chat.common.core.exception.ErrorCode;

/**
 * 채팅 도메인 비즈니스 규칙 위반 예외
 *
 * Phase 4: DomainException 대체 - chat-domain 의존성 제거
 * BaseException 상속으로 공통 예외 체계에 통합
 */
public class ChatException extends BaseException {

    public ChatException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ChatException(ErrorCode errorCode, Object[] args) {
        super(errorCode, args);
    }

    public ChatException(ErrorCode errorCode, Object[] args, Throwable cause) {
        super(errorCode, args, cause);
    }
}
