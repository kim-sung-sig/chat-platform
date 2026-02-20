package com.example.chat.domain.schedule

import com.example.chat.domain.message.Message
import java.time.Instant

/**
 * 테스트에서 사용하는 최소한의 ScheduleRule 구현
 */
class ScheduleRule(
    private val type: ScheduleType,
    private val message: Message,
    private val scheduledAt: Instant?,
    private val cronExpression: CronExpression?
) {
    fun getType(): ScheduleType = type
    fun getMessage(): Message = message
    fun getScheduledAt(): Instant? = scheduledAt
    fun getCronExpression(): CronExpression? = cronExpression
}
