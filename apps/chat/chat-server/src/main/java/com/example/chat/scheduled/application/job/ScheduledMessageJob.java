package com.example.chat.scheduled.application.job;

import com.example.chat.scheduled.application.service.ScheduledMessageCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

/**
 * Quartz Job — 예약 발송 실행 단위
 *
 * scheduledAt 도달 시 Quartz Scheduler가 이 Job을 실행한다.
 * JobDataMap에서 scheduledMessageId를 꺼내 CommandService에 위임.
 * @DisallowConcurrentExecution: 동일 Job 인스턴스의 중복 실행 방지
 */
@Component
@RequiredArgsConstructor
@Slf4j
@DisallowConcurrentExecution
public class ScheduledMessageJob implements Job {

    public static final String KEY_SCHEDULED_MESSAGE_ID = "scheduledMessageId";

    private final ScheduledMessageCommandService commandService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap data = context.getMergedJobDataMap();
        String scheduledMessageId = data.getString(KEY_SCHEDULED_MESSAGE_ID);

        log.info("ScheduledMessageJob executing: scheduledMessageId={}", scheduledMessageId);

        try {
            commandService.executeScheduledMessage(scheduledMessageId);
        } catch (Exception e) {
            log.error("ScheduledMessageJob failed: scheduledMessageId={}", scheduledMessageId, e);
            throw new JobExecutionException(e, /* refireImmediately= */ false);
        }
    }
}
