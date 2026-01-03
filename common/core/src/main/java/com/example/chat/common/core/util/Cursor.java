package com.example.chat.common.core.util;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 커서 기반 페이지네이션을 위한 Cursor 클래스
 *
 * @param <T> 커서 값의 타입
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
