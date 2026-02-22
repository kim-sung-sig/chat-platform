package com.example.chat.domain.schedule;

import java.time.Instant;

import com.example.chat.domain.message.Message;

/**
 * 테스트에서 사용하는 최소한의 ScheduleRule 구현
 */
public record ScheduleRule(
        ScheduleType type,
        Message message,
        Instant scheduledAt,
        CronExpression cronExpression) {

    // JavaBean 호환 getters
    public ScheduleType getType() { return type(); }
    public Message getMessage() { return message(); }
    public Instant getScheduledAt() { return scheduledAt(); }
    public CronExpression getCronExpression() { return cronExpression(); }
}
