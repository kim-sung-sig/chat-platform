package com.example.chat.domain.service;

import com.example.chat.domain.message.Message;
import com.example.chat.domain.schedule.CronExpression;
import com.example.chat.domain.schedule.ScheduleRule;

import java.time.Instant;

/**
 * 스케줄 도메인 서비스
 */
public class ScheduleDomainService {

    /**
     * 단발성 스케줄 생성
     */
    public ScheduleRule createOneTimeSchedule(Message message, Instant scheduledAt) {
        validateScheduledTime(scheduledAt);
        return ScheduleRule.oneTime(message, scheduledAt);
    }

    /**
     * 주기적 스케줄 생성
     */
    public ScheduleRule createRecurringSchedule(Message message, CronExpression cronExpression) {
        if (cronExpression == null) {
            throw new IllegalArgumentException("Cron expression cannot be null");
        }
        return ScheduleRule.recurring(message, cronExpression);
    }

    /**
     * 스케줄 시간 검증
     */
    private void validateScheduledTime(Instant scheduledAt) {
        if (scheduledAt == null) {
            throw new IllegalArgumentException("Scheduled time cannot be null");
        }
        if (scheduledAt.isBefore(Instant.now())) {
            throw new IllegalArgumentException("Scheduled time must be in the future");
        }

        // 너무 먼 미래는 제한 (예: 1년)
        Instant oneYearLater = Instant.now().plusSeconds(365 * 24 * 60 * 60);
        if (scheduledAt.isAfter(oneYearLater)) {
            throw new IllegalArgumentException("Scheduled time cannot be more than 1 year in the future");
        }
    }
}
