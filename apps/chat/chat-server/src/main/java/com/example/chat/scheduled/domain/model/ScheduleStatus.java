package com.example.chat.scheduled.domain.model;

/**
 * 예약 발송 상태
 *
 * 상태 전이:
 *   PENDING → EXECUTING → EXECUTED
 *                       → FAILED (retryCount < 3 → PENDING 재스케줄)
 *   PENDING → CANCELLED
 */
public enum ScheduleStatus {
    /** 발송 대기 중 */
    PENDING,
    /** 발송 실행 중 */
    EXECUTING,
    /** 발송 완료 */
    EXECUTED,
    /** 발송 취소 */
    CANCELLED,
    /** 최대 재시도 초과 실패 */
    FAILED
}
