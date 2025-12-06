package com.example.chat.storage.domain.message;

import java.util.Map;

/**
 * 메시지 콘텐츠 인터페이스
 * 전략 패턴 - 다양한 메시지 타입별 콘텐츠 구현
 */
public interface MessageContent {

    /**
     * 메시지 타입 반환
     */
    MessageType getType();

    /**
     * JSON으로 직렬화
     */
    String toJson();

    /**
     * 콘텐츠 검증
     * @throws IllegalArgumentException 검증 실패 시
     */
    void validate();

    /**
     * 메타데이터 반환 (검색, 필터링 등에 사용)
     */
    Map<String, Object> getMetadata();

    /**
     * 콘텐츠 요약 (알림 등에 사용)
     * @param maxLength 최대 길이
     * @return 요약된 콘텐츠
     */
    String getSummary(int maxLength);
}
