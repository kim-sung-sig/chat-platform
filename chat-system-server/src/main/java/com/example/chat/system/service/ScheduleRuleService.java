package com.example.chat.system.service;

import com.example.chat.system.domain.entity.Message;
import com.example.chat.system.domain.entity.ScheduleRule;
import com.example.chat.system.domain.enums.ScheduleType;
import com.example.chat.system.dto.request.ScheduleCreateRequest;
import com.example.chat.system.dto.response.ScheduleRuleResponse;
import com.example.chat.system.exception.BusinessException;
import com.example.chat.system.exception.ResourceNotFoundException;
import com.example.chat.system.infrastructure.scheduler.QuartzSchedulerService;
import com.example.chat.system.repository.MessageRepository;
import com.example.chat.system.repository.ScheduleRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 스케줄 관리 서비스
 * 책임: 스케줄 규칙 CRUD, 스케줄 활성화/비활성화, 실행 시간 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleRuleService {

    private final ScheduleRuleRepository scheduleRuleRepository;
    private final MessageRepository messageRepository;
    private final MessageService messageService;
    private final QuartzSchedulerService quartzSchedulerService;

    /**
     * 스케줄 생성
     */
    @Transactional
    public ScheduleRuleResponse createSchedule(ScheduleCreateRequest request) {
        log.info("Creating schedule for message: {}", request.getMessageId());

        // 메시지 조회 및 검증
        Message message = messageRepository.findById(request.getMessageId())
                .orElseThrow(() -> new ResourceNotFoundException("Message", request.getMessageId()));

        // 스케줄 타입별 검증
        validateScheduleRequest(request);

        // 메시지 상태를 SCHEDULED로 변경
        messageService.prepareForPublish(message.getId());

        // Job 이름 생성
        String jobName = generateJobName(message.getId());
        String jobGroup = "MESSAGE_PUBLISH_GROUP";

        // 스케줄 생성
        ScheduleRule scheduleRule = ScheduleRule.builder()
                .message(message)
                .scheduleType(request.getScheduleType())
                .cronExpression(request.getCronExpression())
                .executionTime(request.getExecutionTime())
                .nextExecutionTime(calculateNextExecutionTime(request))
                .isActive(true)
                .jobName(jobName)
                .jobGroup(jobGroup)
                .executionCount(0)
                .maxExecutionCount(request.getMaxExecutionCount())
                .build();

        ScheduleRule savedSchedule = scheduleRuleRepository.save(scheduleRule);
        log.info("Schedule created successfully: {}", savedSchedule.getId());
        // Quartz Scheduler에 Job 등록
        try {
            if (request.getScheduleType() == ScheduleType.ONCE) {
                quartzSchedulerService.scheduleOnceJob(
                    jobName, jobGroup,
                    message.getId(),
                    savedSchedule.getId(),
                    request.getExecutionTime()
                );
            } else {
                quartzSchedulerService.scheduleRecurringJob(
                    jobName, jobGroup,
                    message.getId(),
                    savedSchedule.getId(),
                    request.getCronExpression()
                );
            }
            log.info("Quartz job registered successfully: {}", jobName);
        } catch (Exception e) {
            log.error("Failed to register Quartz job", e);
            throw new com.example.chat.system.exception.SchedulingException("스케줄 등록 실패", e);
        }


        return ScheduleRuleResponse.from(savedSchedule);
    }

    /**
     * 스케줄 조회
     */
    public ScheduleRuleResponse getSchedule(Long scheduleId) {
        ScheduleRule scheduleRule = scheduleRuleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("ScheduleRule", scheduleId));
        return ScheduleRuleResponse.from(scheduleRule);
    }

    /**
     * 메시지별 스케줄 목록 조회
     */
    public List<ScheduleRuleResponse> getSchedulesByMessage(Long messageId) {
        return scheduleRuleRepository.findByMessageId(messageId).stream()
                .map(ScheduleRuleResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 활성화된 스케줄 목록 조회
     */
    public List<ScheduleRuleResponse> getActiveSchedules() {
        return scheduleRuleRepository.findByIsActiveTrue().stream()
                .map(ScheduleRuleResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 실행 대상 스케줄 조회
     */
    public List<ScheduleRule> getExecutableSchedules() {
        return scheduleRuleRepository.findExecutableSchedules(LocalDateTime.now());
    }

    /**
     * 스케줄 활성화
     */
    @Transactional
    public void activateSchedule(Long scheduleId) {
        log.info("Activating schedule: {}", scheduleId);

        ScheduleRule scheduleRule = scheduleRuleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("ScheduleRule", scheduleId));

        scheduleRule.activate();
        log.info("Schedule activated: {}", scheduleId);
    }

    /**
     * 스케줄 비활성화
     */
    @Transactional
    public void deactivateSchedule(Long scheduleId) {
        log.info("Deactivating schedule: {}", scheduleId);

        ScheduleRule scheduleRule = scheduleRuleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("ScheduleRule", scheduleId));

        scheduleRule.deactivate();
        log.info("Schedule deactivated: {}", scheduleId);
    }

    /**
     * 스케줄 실행 완료 처리
     */
    @Transactional
    public void markAsExecuted(Long scheduleId, LocalDateTime nextExecutionTime) {
        log.info("Marking schedule as executed: {}", scheduleId);

        ScheduleRule scheduleRule = scheduleRuleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("ScheduleRule", scheduleId));

        scheduleRule.markAsExecuted();

        if (scheduleRule.getScheduleType() == ScheduleType.RECURRING && nextExecutionTime != null) {
            scheduleRule.updateNextExecutionTime(nextExecutionTime);
        }

        log.info("Schedule marked as executed: {}", scheduleId);
    }

    /**
     * 스케줄 삭제
     */
    @Transactional
    public void deleteSchedule(Long scheduleId) {
        log.info("Deleting schedule: {}", scheduleId);

        ScheduleRule scheduleRule = scheduleRuleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("ScheduleRule", scheduleId));

        // Quartz Job 삭제
        try {
            quartzSchedulerService.deleteJob(scheduleRule.getJobName(), scheduleRule.getJobGroup());
            log.info("Quartz job deleted: {}", scheduleRule.getJobName());
        } catch (Exception e) {
            log.error("Failed to delete Quartz job", e);
            // Job 삭제 실패해도 스케줄 규칙은 삭제 (로그만 남김)
        }

        scheduleRuleRepository.delete(scheduleRule);
        log.info("Schedule deleted: {}", scheduleId);
    }

    /**
     * 스케줄 요청 검증
     */
    private void validateScheduleRequest(ScheduleCreateRequest request) {
        if (request.getScheduleType() == ScheduleType.RECURRING) {
            if (request.getCronExpression() == null || request.getCronExpression().isBlank()) {
                throw new BusinessException("주기적 스케줄에는 Cron 표현식이 필수입니다");
            }
        } else if (request.getScheduleType() == ScheduleType.ONCE) {
            if (request.getExecutionTime() == null) {
                throw new BusinessException("단발성 스케줄에는 실행 시간이 필수입니다");
            }
            if (request.getExecutionTime().isBefore(LocalDateTime.now())) {
                throw new BusinessException("실행 시간은 현재 시간 이후여야 합니다");
            }
        }
    }

    /**
     * 다음 실행 시간 계산
     */
    private LocalDateTime calculateNextExecutionTime(ScheduleCreateRequest request) {
        if (request.getScheduleType() == ScheduleType.ONCE) {
            return request.getExecutionTime();
        }
        // RECURRING의 경우 Quartz가 Cron 표현식을 기반으로 계산
        // 여기서는 임시로 현재 시간 반환 (실제로는 Quartz Scheduler에서 처리)
        return LocalDateTime.now();
    }

    /**
     * Job 이름 생성
     */
    private String generateJobName(Long messageId) {
        return "MESSAGE_PUBLISH_JOB_" + messageId + "_" + UUID.randomUUID();
    }
}