package com.example.chat.system.exception;

/**
 * 스케줄링 처리 중 발생하는 예외
 */
public class SchedulingException extends RuntimeException {

    public SchedulingException(String message) {
        super(message);
    }

    public SchedulingException(String message, Throwable cause) {
        super(message, cause);
    }
}