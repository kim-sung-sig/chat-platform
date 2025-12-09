package com.example.chat.domain.common;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * 커서 기반 페이징을 위한 Cursor (Value Object)
 */
@Getter
@EqualsAndHashCode
@ToString
public class Cursor {
    private final String value;

    private Cursor(String value) {
        this.value = value;
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
        return null;  // null은 시작을 의미
    }

    public boolean isStart() {
        return this.value == null;
    }
}
