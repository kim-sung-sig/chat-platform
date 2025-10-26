package com.example.chat.system.infrastructure.scheduler;

import com.example.chat.system.domain.entity.Message;
import com.example.chat.system.repository.MessageRepository;
import com.example.chat.system.service.MessagePublisherService;
import com.example.chat.system.service.MessageService;
import com.example.chat.system.service.ScheduleRuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.springframework.stereotype.Component;

/**
 * Quartz Job - 메시지 발행 Job
 * Quartz 스케줄러가 실행하는 실제 작업
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessagePublishJob implements Job {

    private final MessagePublisherService messagePublisherService;
    private final MessageRepository messageRepository;
    private final MessageService messageService;
    private final ScheduleRuleService scheduleRuleService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            // JobDataMap에서 데이터 추출
            JobDetail jobDetail = context.getJobDetail();
            JobKey jobKey = jobDetail.getKey();
            String description = jobDetail.getDescription();
            log.debug("Executing Job: {} - {}", jobKey, description);

            JobDataMap jobDataMap = jobDetail.getJobDataMap();
            Long messageId = jobDataMap.getLong("messageId");
            Long scheduleRuleId = jobDataMap.getLong("scheduleRuleId");

            log.info("Executing MessagePublishJob - messageId: {}, scheduleRuleId: {}",
                    messageId, scheduleRuleId);

            // Message 조회
            Message message = messageRepository.findById(messageId)
                    .orElseThrow(() -> new RuntimeException("Message not found: " + messageId));

            // 메시지 발행
            messagePublisherService.publishMessage(message, scheduleRuleId);

            // 메시지 상태를 PUBLISHED로 변경
            messageService.markAsPublished(messageId);

            // 스케줄 실행 완료 처리
            // TODO: 다음 실행 시간 계산 로직 필요 (CronExpression 사용)
            scheduleRuleService.markAsExecuted(scheduleRuleId, null);

            log.info("MessagePublishJob completed successfully");

        } catch (Exception e) {
            log.error("Error executing MessagePublishJob", e);
            throw new JobExecutionException(e);
        }
    }
}