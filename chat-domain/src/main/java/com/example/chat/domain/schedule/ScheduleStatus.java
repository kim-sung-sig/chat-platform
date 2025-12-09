package com.example.chat.domain.schedule;

/**
 * 스케줄 상태
 */
public enum ScheduleStatus {
    /**
     * 대기 중 (아직 실행되지 않음)
     */
    PENDING,

    /**
     * 활성 (실행 가능 상태)
     */
    ACTIVE,

    /**
     * 실행 완료 (단발성 스케줄)
     */
    EXECUTED,

    /**
     * 취소됨
     */
    CANCELLED,

    /**
     * 실패
     */
    FAILED
}
