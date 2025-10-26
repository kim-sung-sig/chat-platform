package com.example.chat.system.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 커서 기반 페이징 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CursorPageResponse<T> {

    private List<T> content;
    private Long nextCursor; // 다음 페이지 커서
    private Boolean hasNext; // 다음 페이지 존재 여부
    private Integer size; // 요청한 페이지 크기

    /**
     * 커서 기반 페이징 응답 생성
     */
    public static <T> CursorPageResponse<T> of(List<T> content, Long nextCursor, Boolean hasNext, Integer size) {
        return CursorPageResponse.<T>builder()
                .content(content)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .size(size)
                .build();
    }
}