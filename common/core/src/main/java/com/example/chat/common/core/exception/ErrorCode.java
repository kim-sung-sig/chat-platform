package com.example.chat.common.core.exception;

/**
 * 에러 코드 인터페이스
 */
public interface ErrorCode {
    String getCode();

    String getMessage();

    int getStatus();
}
