package com.example.chat.message.application.dto.response;

import java.util.List;

/**
 * Cursor 기반 페이징 응답 DTO
 *
 * @param items      조회된 아이템 목록
 * @param nextCursor 다음 페이지 커서 (null 이면 마지막 페이지)
 * @param hasNext    다음 페이지 존재 여부
 * @param size       현재 페이지 크기
 */
public record CursorPageResponse<T>(
        List<T> items,
        String nextCursor,
        boolean hasNext,
        int size) {
}
