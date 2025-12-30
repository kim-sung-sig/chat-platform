package com.example.chat.system.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 커서 기반 페이징 응답 DTO
 *
 * Generic Type: 페이징될 데이터의 타입
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CursorPageResponse<T> {

    /**
     * 페이징된 데이터 목록
     */
    private List<T> data;

    /**
     * 다음 페이지 커서 (Base64 인코딩된 문자열)
     */
    private String nextCursor;

    /**
     * 다음 페이지 존재 여부
     */
    private Boolean hasNext;

    /**
     * 현재 페이지 데이터 개수
     */
    private Integer size;

    /**
     * 커서 기반 페이징 응답 생성
     */
    public static <T> CursorPageResponse<T> of(List<T> data, String nextCursor, Boolean hasNext) {
        return CursorPageResponse.<T>builder()
                .data(data)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .size(data.size())
                .build();
    }
}
