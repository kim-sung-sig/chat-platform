package com.example.chat.common.util.util;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Cursor 기반 페이징을 위한 VO
 *
 * @param <T> Cursor 값의 타입 (Long, String 등)
 */
@Getter
@NoArgsConstructor
public class Cursor<T> {

    private T value;
    private boolean hasNext;

    private Cursor(T value, boolean hasNext) {
        this.value = value;
        this.hasNext = hasNext;
    }

    public static <T> Cursor<T> of(T value, boolean hasNext) {
        return new Cursor<>(value, hasNext);
    }

    public static <T> Cursor<T> empty() {
        return new Cursor<>(null, false);
    }

    public boolean isEmpty() {
        return value == null;
    }
}
