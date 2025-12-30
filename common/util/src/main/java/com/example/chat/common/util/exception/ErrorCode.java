package com.example.chat.common.util.exception;

/**
 * 에러 코드 인터페이스
 * 모든 에러 코드는 이 인터페이스를 구현해야 함
 */
public interface ErrorCode {
    String getCode();
    String getMessage();
    int getStatus();
}
