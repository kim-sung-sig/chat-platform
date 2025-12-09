package com.example.chat.domain.schedule;

/**
 * 스케줄 타입
 */
public enum ScheduleType {
    /**
     * 단발성 스케줄 (한 번만 실행)
     */
    ONE_TIME,

    /**
     * 주기적 스케줄 (반복 실행)
     */
    RECURRING
}
