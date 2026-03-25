package com.example.chat.scheduled.infrastructure.quartz;

import com.example.chat.scheduled.application.job.ScheduledMessageJob;
import com.example.chat.scheduled.domain.model.ScheduledMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.TimeZone;

/**
 * Quartz Scheduler 어댑터
 *
 * ScheduledMessage를 Quartz Job으로 등록/삭제한다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class QuartzJobScheduler {

    private final Scheduler scheduler;

    /**
     * 예약 Job 등록
     *
     * @param domain 등록할 ScheduledMessage
     */
    public void schedule(ScheduledMessage domain) throws SchedulerException {
        JobDetail job = JobBuilder.newJob(ScheduledMessageJob.class)
                .withIdentity(jobKey(domain.getId()))
                .usingJobData(ScheduledMessageJob.KEY_SCHEDULED_MESSAGE_ID, domain.getId())
                .storeDurably(false)
                .build();

        Date triggerAt = Date.from(domain.getScheduledAt().toInstant());

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(triggerKey(domain.getId()))
                .startAt(triggerAt)
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
                .build();

        scheduler.scheduleJob(job, trigger);
        log.info("Quartz job scheduled: id={}, at={}", domain.getId(), domain.getScheduledAt());
    }

    /**
     * 예약 Job 취소
     *
     * @param scheduledMessageId 취소할 예약 ID
     */
    public void unschedule(String scheduledMessageId) throws SchedulerException {
        boolean deleted = scheduler.deleteJob(jobKey(scheduledMessageId));
        log.info("Quartz job unscheduled: id={}, deleted={}", scheduledMessageId, deleted);
    }

    /**
     * 실패 후 재시도 Job 등록 (기존 Job이 소멸된 이후 새 Job으로 재등록)
     *
     * @param scheduledMessageId 재시도할 예약 ID
     * @param retryAt            재시도 시각
     */
    public void scheduleRetry(String scheduledMessageId, ZonedDateTime retryAt) throws SchedulerException {
        JobDetail job = JobBuilder.newJob(ScheduledMessageJob.class)
                .withIdentity(jobKey(scheduledMessageId))
                .usingJobData(ScheduledMessageJob.KEY_SCHEDULED_MESSAGE_ID, scheduledMessageId)
                .storeDurably(false)
                .build();

        Date triggerAt = Date.from(retryAt.toInstant());
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(triggerKey(scheduledMessageId))
                .startAt(triggerAt)
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
                .build();

        scheduler.scheduleJob(job, trigger);
        log.info("Quartz job retry scheduled: id={}, retryAt={}", scheduledMessageId, retryAt);
    }

    // ── helpers ───────────────────────────────────────────────────────

    private static JobKey jobKey(String id) {
        return JobKey.jobKey("scheduled-msg-" + id, "scheduled-message");
    }

    private static TriggerKey triggerKey(String id) {
        return TriggerKey.triggerKey("trigger-" + id, "scheduled-message");
    }
}
