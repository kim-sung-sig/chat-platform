package com.example.chat.system.exception;

/**
 * 스케줄링 관련 예외
 */
public class SchedulingException extends RuntimeException {
    public SchedulingException(String message) {
        super(message);
    }

    public SchedulingException(String message, Throwable cause) {
        super(message, cause);
    }
}
