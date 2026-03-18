package com.example.chat.channel.application.model;

import java.time.Instant;
import java.util.List;

/**
 * 커서 기반 페이징 응답 래퍼.
 *
 * @param content    현재 페이지 항목 목록
 * @param nextCursor 다음 페이지 요청 시 cursor 파라미터로 전달할 값
 *                   (마지막 항목의 createdAt). 더 이상 데이터가 없으면 null.
 * @param hasNext    다음 페이지 존재 여부
 */
public record CursorPage<T>(
        List<T> content,
        Instant nextCursor,
        boolean hasNext) {

    /**
     * size 개 초과 항목이 있는 경우 hasNext = true 로 설정한다.
     * content 에는 size 개까지만 포함되므로 호출 측은 size+1 개를 조회한 후
     * 이 팩토리를 사용한다.
     */
    public static <T> CursorPage<T> of(List<T> fetchedWithExtra, int size, java.util.function.Function<T, Instant> cursorExtractor) {
        boolean hasNext = fetchedWithExtra.size() > size;
        List<T> content = hasNext ? fetchedWithExtra.subList(0, size) : fetchedWithExtra;
        Instant nextCursor = hasNext ? cursorExtractor.apply(content.get(content.size() - 1)) : null;
        return new CursorPage<>(content, nextCursor, hasNext);
    }

    /** 빈 페이지 */
    public static <T> CursorPage<T> empty() {
        return new CursorPage<>(List.of(), null, false);
    }
}
