package com.example.chat.domain.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import com.example.chat.domain.message.Message;
import com.example.chat.domain.schedule.CronExpression;
import com.example.chat.domain.schedule.ScheduleRule;
import com.example.chat.domain.schedule.ScheduleType;

/**
 * 테스트에서 사용되는 ScheduleDomainService의 최소 구현
 */
public class ScheduleDomainService {

    public ScheduleRule createOneTimeSchedule(Message message, Instant scheduledAt) {
        if (scheduledAt == null) {
            throw new IllegalArgumentException("Scheduled time cannot be null");
        }
        Instant now = Instant.now();
        if (!scheduledAt.isAfter(now)) {
            throw new IllegalArgumentException("Scheduled time must be in the future");
        }
        if (scheduledAt.isAfter(now.plus(365, ChronoUnit.DAYS))) {
            throw new IllegalArgumentException("Scheduled time cannot be more than 1 year in the future");
        }

        return new ScheduleRule(ScheduleType.ONE_TIME, message, scheduledAt, null);
    }

    public ScheduleRule createRecurringSchedule(Message message, CronExpression cronExpression) {
        if (cronExpression == null) {
            throw new IllegalArgumentException("Cron expression cannot be null");
        }
        return new ScheduleRule(ScheduleType.RECURRING, message, null, cronExpression);
    }
}
