package com.example.chat.system.infrastructure.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Quartz 스케줄러 관리 서비스
 * 책임: Quartz Job 등록, 수정, 삭제
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuartzSchedulerService {

    private final Scheduler scheduler;

    /**
     * 단발성 스케줄 등록
     */
    public void scheduleOnceJob(String jobName, String jobGroup, Long messageId, Long scheduleRuleId, LocalDateTime executionTime) {
        try {
            JobDetail jobDetail = JobBuilder.newJob(MessagePublishJob.class)
                    .withIdentity(jobName, jobGroup)
                    .usingJobData("messageId", messageId)
                    .usingJobData("scheduleRuleId", scheduleRuleId)
                    .build();

            Date triggerTime = Date.from(executionTime.atZone(ZoneId.systemDefault()).toInstant());

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(jobName + "_TRIGGER", jobGroup)
                    .startAt(triggerTime)
                    .build();

            scheduler.scheduleJob(jobDetail, trigger);
            log.info("Scheduled once job: {} at {}", jobName, executionTime);

        } catch (SchedulerException e) {
            log.error("Failed to schedule once job: {}", jobName, e);
            throw new RuntimeException("Failed to schedule job", e);
        }
    }

    /**
     * 주기적 스케줄 등록 (Cron)
     */
    public void scheduleRecurringJob(String jobName, String jobGroup, Long messageId, Long scheduleRuleId, String cronExpression) {
        try {
            JobDetail jobDetail = JobBuilder.newJob(MessagePublishJob.class)
                    .withIdentity(jobName, jobGroup)
                    .usingJobData("messageId", messageId)
                    .usingJobData("scheduleRuleId", scheduleRuleId)
                    .build();

            CronTrigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(jobName + "_TRIGGER", jobGroup)
                    .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                    .build();

            scheduler.scheduleJob(jobDetail, trigger);
            log.info("Scheduled recurring job: {} with cron: {}", jobName, cronExpression);

        } catch (SchedulerException e) {
            log.error("Failed to schedule recurring job: {}", jobName, e);
            throw new RuntimeException("Failed to schedule job", e);
        }
    }

    /**
     * 스케줄 삭제
     */
    public void deleteJob(String jobName, String jobGroup) {
        try {
            JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
            scheduler.deleteJob(jobKey);
            log.info("Deleted job: {}", jobName);

        } catch (SchedulerException e) {
            log.error("Failed to delete job: {}", jobName, e);
            throw new RuntimeException("Failed to delete job", e);
        }
    }

    /**
     * 스케줄 일시정지
     */
    public void pauseJob(String jobName, String jobGroup) {
        try {
            JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
            scheduler.pauseJob(jobKey);
            log.info("Paused job: {}", jobName);

        } catch (SchedulerException e) {
            log.error("Failed to pause job: {}", jobName, e);
            throw new RuntimeException("Failed to pause job", e);
        }
    }

    /**
     * 스케줄 재개
     */
    public void resumeJob(String jobName, String jobGroup) {
        try {
            JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
            scheduler.resumeJob(jobKey);
            log.info("Resumed job: {}", jobName);

        } catch (SchedulerException e) {
            log.error("Failed to resume job: {}", jobName, e);
            throw new RuntimeException("Failed to resume job", e);
        }
    }
}