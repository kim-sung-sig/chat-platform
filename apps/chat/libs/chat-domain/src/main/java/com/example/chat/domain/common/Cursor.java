package com.example.chat.domain.common;

/**
 * 커서 기반 페이징을 위한 Cursor (Value Object)
 */
public record Cursor(String value) {
    /**
     * 시작 커서인지 확인
     */
    public boolean isStart() {
        return value == null;
    }

    public static Cursor of(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return new Cursor(value);
    }

    /**
     * 시작 커서 (첫 페이지)
     */
    public static Cursor start() {
        return null; // null은 시작을 의미
    }
}
