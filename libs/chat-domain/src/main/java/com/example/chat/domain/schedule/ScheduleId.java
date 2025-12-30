package com.example.chat.domain.schedule;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

/**
 * 스케줄 ID (Value Object)
 */
@Getter
@EqualsAndHashCode
@ToString
public class ScheduleId {
    private final String value;

    private ScheduleId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("ScheduleId cannot be null or blank");
        }
        this.value = value;
    }

    public static ScheduleId of(String value) {
        return new ScheduleId(value);
    }

    public static ScheduleId generate() {
        return new ScheduleId(UUID.randomUUID().toString());
    }
}
