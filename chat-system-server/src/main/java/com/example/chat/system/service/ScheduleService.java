package com.example.chat.system.service;

import com.example.chat.common.auth.context.UserContextHolder;
import com.example.chat.common.auth.model.UserId;
import com.example.chat.domain.schedule.ScheduleRule;
import com.example.chat.domain.schedule.ScheduleRuleRepository;
import com.example.chat.system.dto.request.CreateOneTimeScheduleRequest;
import com.example.chat.system.dto.request.CreateRecurringScheduleRequest;
import com.example.chat.system.dto.response.ScheduleResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 스케줄 서비스
 * Key 기반 도메인 조회 후 조립 패턴 적용
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleService {

	private final ScheduleRuleRepository scheduleRuleRepository;
	private final Scheduler quartzScheduler;
	private final ObjectMapper objectMapper;

	/**
	 * 단발성 스케줄 생성
	 */
	@Transactional
	public ScheduleResponse createOneTimeSchedule(CreateOneTimeScheduleRequest request) {
		log.info("Creating one-time schedule: roomId={}, executeAt={}",
				request.getRoomId(), request.getExecuteAt());

		// Step 1: 사용자 ID 조회 (Key)
		UserId senderId = getUserId();

		// Step 2: 도메인 생성 (팩토리 메서드)
		ScheduleRule rule = ScheduleRule.createOneTime(
				request.getRoomId(),
				request.getChannelId(),
				senderId,
				request.getMessageType(),
				request.getPayload(),
				request.getExecuteAt()
		);

		// Step 3: 영속화
		ScheduleRule savedRule = scheduleRuleRepository.save(rule);

		// Step 4: Quartz Job 등록
		registerQuartzJob(savedRule);

		log.info("One-time schedule created: scheduleId={}", savedRule.getScheduleId());

		// Step 5: Response 변환
		return ScheduleResponse.from(savedRule);
	}

	/**
	 * 주기적 스케줄 생성
	 */
	@Transactional
	public ScheduleResponse createRecurringSchedule(CreateRecurringScheduleRequest request) {
		log.info("Creating recurring schedule: roomId={}, cron={}",
				request.getRoomId(), request.getCronExpression());

		// Step 1: 사용자 ID 조회 (Key)
		UserId senderId = getUserId();

		// Step 2: 도메인 생성 (팩토리 메서드)
		ScheduleRule rule = ScheduleRule.createRecurring(
				request.getRoomId(),
				request.getChannelId(),
				senderId,
				request.getMessageType(),
				request.getPayload(),
				request.getCronExpression(),
				request.getMaxExecutionCount()
		);

		// Step 3: 영속화
		ScheduleRule savedRule = scheduleRuleRepository.save(rule);

		// Step 4: Quartz Job 등록
		registerQuartzJob(savedRule);

		log.info("Recurring schedule created: scheduleId={}", savedRule.getScheduleId());

		// Step 5: Response 변환
		return ScheduleResponse.from(savedRule);
	}

	/**
	 * 스케줄 일시중지
	 */
	@Transactional
	public ScheduleResponse pauseSchedule(Long scheduleId) {
		log.info("Pausing schedule: scheduleId={}", scheduleId);

		// Step 1: Key로 도메인 조회
		ScheduleRule rule = findScheduleRule(scheduleId);

		// Step 2: 도메인 로직 실행
		ScheduleRule pausedRule = rule.pause();

		// Step 3: 영속화
		ScheduleRule savedRule = scheduleRuleRepository.save(pausedRule);

		// Step 4: Quartz Job 일시중지
		pauseQuartzJob(scheduleId);

		return ScheduleResponse.from(savedRule);
	}

	/**
	 * 스케줄 재개
	 */
	@Transactional
	public ScheduleResponse resumeSchedule(Long scheduleId) {
		log.info("Resuming schedule: scheduleId={}", scheduleId);

		// Step 1: Key로 도메인 조회
		ScheduleRule rule = findScheduleRule(scheduleId);

		// Step 2: 도메인 로직 실행
		ScheduleRule resumedRule = rule.resume();

		// Step 3: 영속화
		ScheduleRule savedRule = scheduleRuleRepository.save(resumedRule);

		// Step 4: Quartz Job 재개
		resumeQuartzJob(scheduleId);

		return ScheduleResponse.from(savedRule);
	}

	/**
	 * 스케줄 취소
	 */
	@Transactional
	public void cancelSchedule(Long scheduleId) {
		log.info("Cancelling schedule: scheduleId={}", scheduleId);

		// Step 1: Key로 도메인 조회
		ScheduleRule rule = findScheduleRule(scheduleId);

		// Step 2: 도메인 로직 실행
		ScheduleRule cancelledRule = rule.cancel();

		// Step 3: 영속화
		scheduleRuleRepository.save(cancelledRule);

		// Step 4: Quartz Job 삭제
		deleteQuartzJob(scheduleId);
	}

	/**
	 * 사용자의 스케줄 목록 조회
	 */
	public List<ScheduleResponse> getMySchedules() {
		UserId senderId = getUserId();

		List<ScheduleRule> rules = scheduleRuleRepository
				.findActiveBySenderId(senderId.getValue());

		return rules.stream()
				.map(ScheduleResponse::from)
				.collect(Collectors.toList());
	}

	/**
	 * 채팅방의 스케줄 목록 조회
	 */
	public List<ScheduleResponse> getSchedulesByRoom(String roomId) {
		List<ScheduleRule> rules = scheduleRuleRepository
				.findActiveByRoomId(roomId);

		return rules.stream()
				.map(ScheduleResponse::from)
				.collect(Collectors.toList());
	}

	// ========== Private Helper Methods ==========

	/**
	 * 인증된 사용자 ID 조회
	 */
	private UserId getUserId() {
		UserId userId = UserContextHolder.getUserId();

		// Early return: 인증되지 않은 사용자
		if (userId == null) {
			throw new IllegalStateException("User not authenticated");
		}

		return userId;
	}

	/**
	 * Key로 ScheduleRule 조회
	 */
	private ScheduleRule findScheduleRule(Long scheduleId) {
		return scheduleRuleRepository.findById(scheduleId)
				.orElseThrow(() -> new IllegalArgumentException(
						"Schedule not found: " + scheduleId
				));
	}

	/**
	 * Quartz Job 등록
	 */
	private void registerQuartzJob(ScheduleRule rule) {
		try {
			JobDetail jobDetail = JobBuilder
					.newJob()
					.ofType(com.example.chat.system.job.MessagePublishJob.class)
					.withIdentity("schedule-" + rule.getScheduleId())
					.usingJobData("scheduleId", rule.getScheduleId())
					.storeDurably()
					.build();

			Trigger trigger = createTrigger(rule);

			quartzScheduler.scheduleJob(jobDetail, trigger);

			log.info("Quartz job registered: scheduleId={}", rule.getScheduleId());

		} catch (SchedulerException e) {
			log.error("Failed to register Quartz job: scheduleId={}",
					rule.getScheduleId(), e);
			throw new RuntimeException("Failed to register schedule", e);
		}
	}

	/**
	 * Trigger 생성
	 */
	private Trigger createTrigger(ScheduleRule rule) {
		TriggerBuilder<Trigger> builder = TriggerBuilder.newTrigger()
				.withIdentity("trigger-" + rule.getScheduleId());

		if (rule.getType() == com.example.chat.storage.domain.schedule.ScheduleType.ONE_TIME) {
			// 단발성: SimpleSchedule
			Date startTime = Date.from(
					rule.getExecuteAt().atZone(ZoneId.systemDefault()).toInstant()
			);
			builder.startAt(startTime);

		} else {
			// 주기적: CronSchedule
			builder.withSchedule(
					CronScheduleBuilder.cronSchedule(rule.getCronExpression())
			);
		}

		return builder.build();
	}

	/**
	 * Quartz Job 일시중지
	 */
	private void pauseQuartzJob(Long scheduleId) {
		try {
			JobKey jobKey = new JobKey("schedule-" + scheduleId);
			quartzScheduler.pauseJob(jobKey);

		} catch (SchedulerException e) {
			log.error("Failed to pause Quartz job: scheduleId={}", scheduleId, e);
		}
	}

	/**
	 * Quartz Job 재개
	 */
	private void resumeQuartzJob(Long scheduleId) {
		try {
			JobKey jobKey = new JobKey("schedule-" + scheduleId);
			quartzScheduler.resumeJob(jobKey);

		} catch (SchedulerException e) {
			log.error("Failed to resume Quartz job: scheduleId={}", scheduleId, e);
		}
	}

	/**
	 * Quartz Job 삭제
	 */
	private void deleteQuartzJob(Long scheduleId) {
		try {
			JobKey jobKey = new JobKey("schedule-" + scheduleId);
			quartzScheduler.deleteJob(jobKey);

		} catch (SchedulerException e) {
			log.error("Failed to delete Quartz job: scheduleId={}", scheduleId, e);
		}
	}
}
