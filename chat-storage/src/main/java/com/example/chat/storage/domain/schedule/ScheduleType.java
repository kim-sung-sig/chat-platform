package com.example.chat.storage.domain.schedule;

import lombok.Getter;

/**
 * 스케줄 타입 Enum
 */
@Getter
public enum ScheduleType {
    /**
     * 단발성 (1회만 실행)
     */
    ONE_TIME("one_time", "단발성"),

    /**
     * 주기적 (Cron 표현식)
     */
    RECURRING("recurring", "주기적");

    private final String code;
    private final String description;

    ScheduleType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static ScheduleType fromCode(String code) {
        for (ScheduleType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown schedule type code: " + code);
    }
}
