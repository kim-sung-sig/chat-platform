package com.example.chat.domain.schedule;

/**
 * 단순한 Cron 표현식 래퍼 (테스트용 최소 구현)
 */
public record CronExpression(String value) {
    public static CronExpression of(String value) {
        return new CronExpression(value);
    }

    public String getValue() { return value(); }
}
