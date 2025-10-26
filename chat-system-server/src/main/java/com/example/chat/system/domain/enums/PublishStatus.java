package com.example.chat.system.domain.enums;

/**
 * 발행 상태
 */
public enum PublishStatus {
    PENDING,  // 발행 대기
    SUCCESS,  // 발행 성공
    FAILED,   // 발행 실패
    RETRY     // 재시도 중
}