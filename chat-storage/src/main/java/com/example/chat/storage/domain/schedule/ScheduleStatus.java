package com.example.chat.storage.domain.schedule;

import lombok.Getter;

/**
 * 스케줄 상태 Enum
 */
@Getter
public enum ScheduleStatus {
    /**
     * 활성 (실행 대기 중)
     */
    ACTIVE("active", "활성"),

    /**
     * 일시중지
     */
    PAUSED("paused", "일시중지"),

    /**
     * 완료 (모든 실행 완료)
     */
    COMPLETED("completed", "완료"),

    /**
     * 취소됨
     */
    CANCELLED("cancelled", "취소됨");

    private final String code;
    private final String description;

    ScheduleStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static ScheduleStatus fromCode(String code) {
        for (ScheduleStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown schedule status code: " + code);
    }

    /**
     * 실행 가능한 상태인지 확인
     */
    public boolean isExecutable() {
        return this == ACTIVE;
    }
}
