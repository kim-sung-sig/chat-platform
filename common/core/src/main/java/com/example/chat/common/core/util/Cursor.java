package com.example.chat.common.core.util;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Cursor 기반 ?�이징을 ?�한 VO
 *
 * @param <T> Cursor 값의 ?�??(Long, String ??
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
