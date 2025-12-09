package com.example.chat.domain.schedule;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Cron 표현식 (Value Object)
 */
@Getter
@EqualsAndHashCode
@ToString
public class CronExpression {
    private final String value;

    private CronExpression(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("CronExpression cannot be null or blank");
        }
        validateCronExpression(value);
        this.value = value;
    }

    public static CronExpression of(String value) {
        return new CronExpression(value);
    }

    /**
     * Cron 표현식 검증 (간단한 검증)
     * 실제로는 Quartz의 CronExpression.isValidExpression()을 사용하는 것이 좋음
     */
    private void validateCronExpression(String cron) {
        String[] parts = cron.split("\\s+");
        if (parts.length < 6 || parts.length > 7) {
            throw new IllegalArgumentException("Invalid cron expression format");
        }
    }
}
