package com.example.chat.system.domain.enums;

/**
 * 메시지 상태
 */
public enum MessageStatus {
    DRAFT,      // 작성 중
    SCHEDULED,  // 스케줄 등록됨
    PUBLISHED,  // 발행 완료
    CANCELLED   // 취소됨
}