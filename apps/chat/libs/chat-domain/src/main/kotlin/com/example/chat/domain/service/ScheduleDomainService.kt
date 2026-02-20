package com.example.chat.domain.service

import com.example.chat.domain.message.Message
import com.example.chat.domain.schedule.CronExpression
import com.example.chat.domain.schedule.ScheduleRule
import com.example.chat.domain.schedule.ScheduleType
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * 테스트에서 사용되는 ScheduleDomainService의 최소 구현
 */
class ScheduleDomainService {

    fun createOneTimeSchedule(message: Message, scheduledAt: Instant?): ScheduleRule {
        requireNotNull(scheduledAt) { "Scheduled time cannot be null" }
        val now = Instant.now()
        require(scheduledAt.isAfter(now)) { "Scheduled time must be in the future" }
        require(!scheduledAt.isAfter(now.plus(365, ChronoUnit.DAYS))) { "Scheduled time cannot be more than 1 year in the future" }

        return ScheduleRule(ScheduleType.ONE_TIME, message, scheduledAt, null)
    }

    fun createRecurringSchedule(message: Message, cronExpression: CronExpression?): ScheduleRule {
        requireNotNull(cronExpression) { "Cron expression cannot be null" }
        return ScheduleRule(ScheduleType.RECURRING, message, null, cronExpression)
    }
}
