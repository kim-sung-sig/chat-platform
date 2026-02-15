package com.example.chat.system.service;

import com.example.chat.auth.core.util.SecurityUtils;
import com.example.chat.domain.channel.Channel;
import com.example.chat.domain.channel.ChannelId;
import com.example.chat.domain.message.Message;
import com.example.chat.domain.schedule.CronExpression;
import com.example.chat.domain.schedule.ScheduleId;
import com.example.chat.domain.schedule.ScheduleRule;
import com.example.chat.domain.schedule.ScheduleRuleRepository;
import com.example.chat.domain.service.MessageDomainService;
import com.example.chat.domain.service.ScheduleDomainService;
import com.example.chat.domain.user.User;
import com.example.chat.domain.user.UserId;
import com.example.chat.system.dto.request.CreateOneTimeScheduleRequest;
import com.example.chat.system.dto.request.CreateRecurringScheduleRequest;
import com.example.chat.system.dto.response.ScheduleResponse;
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

import java.time.Instant;
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
	private final com.example.chat.domain.channel.ChannelRepository channelRepository;
	private final com.example.chat.domain.user.UserRepository userRepository;
	private final MessageDomainService messageDomainService;
	private final ScheduleDomainService scheduleDomainService;
	private final Scheduler quartzScheduler;

	/**
	 * 단발성 스케줄 생성
	 *
	 * Application Service 패턴:
	 * 1. Key로 Aggregate 조회
	 * 2. Domain Service 호출 (Aggregate 전달)
	 * 3. 영속화
	 * 4. 인프라 작업 (Quartz)
	 */
	@Transactional
	public ScheduleResponse createOneTimeSchedule(CreateOneTimeScheduleRequest request) {
		log.info("Creating one-time schedule: channelId={}, executeAt={}",
				request.getChannelId(), request.getExecuteAt());

		// Step 1: Key 조회 - 인증된 사용자 ID
		UserId senderId = getUserIdFromContext();

		// Step 2: Key로 Aggregate 조회 - Channel
		ChannelId channelId = ChannelId.of(request.getChannelId());
		Channel channel = findChannelById(channelId);

		// Step 3: Key로 Aggregate 조회 - User (Sender)
		User sender = findUserById(senderId);

		// Step 4: Domain Service 호출 - Message 생성 (Aggregate 간 협력)
		Message message = createMessageByType(channel, sender, request.getMessageType(), request.getPayload());

		// Step 5: Instant 변환
		Instant scheduledAt = request.getExecuteAt().atZone(ZoneId.systemDefault()).toInstant();

		// Step 6: Domain Service 호출 - ScheduleRule 생성
		ScheduleRule rule = scheduleDomainService.createOneTimeSchedule(message, scheduledAt);

		// Step 7: 영속화
		ScheduleRule savedRule = scheduleRuleRepository.save(rule);

		// Step 8: 인프라 작업 - Quartz Job 등록
		registerQuartzJob(savedRule);

		log.info("One-time schedule created: scheduleId={}", savedRule.getId().getValue());

		// Step 9: Response 변환
		return ScheduleResponse.from(savedRule);
	}

	/**
	 * 주기적 스케줄 생성
	 */
	@Transactional
	public ScheduleResponse createRecurringSchedule(CreateRecurringScheduleRequest request) {
		log.info("Creating recurring schedule: channelId={}, cron={}",
				request.getChannelId(), request.getCronExpression());

		// Step 1: Key 조회 - 인증된 사용자 ID
		UserId senderId = getUserIdFromContext();

		// Step 2: Key로 Aggregate 조회 - Channel
		ChannelId channelId = ChannelId.of(request.getChannelId());
		Channel channel = findChannelById(channelId);

		// Step 3: Key로 Aggregate 조회 - User (Sender)
		User sender = findUserById(senderId);

		// Step 4: Domain Service 호출 - Message 생성 (Aggregate 간 협력)
		Message message = createMessageByType(channel, sender, request.getMessageType(), request.getPayload());

		// Step 5: CronExpression 생성
		CronExpression cronExpression = CronExpression.of(request.getCronExpression());

		// Step 6: Domain Service 호출 - ScheduleRule 생성
		ScheduleRule rule = scheduleDomainService.createRecurringSchedule(message, cronExpression);

		// Step 7: 영속화
		ScheduleRule savedRule = scheduleRuleRepository.save(rule);

		// Step 8: 인프라 작업 - Quartz Job 등록
		registerQuartzJob(savedRule);

		log.info("Recurring schedule created: scheduleId={}", savedRule.getId().getValue());

		// Step 9: Response 변환
		return ScheduleResponse.from(savedRule);
	}

	/**
	 * 스케줄 취소
	 */
	@Transactional
	public void cancelSchedule(String scheduleIdStr) {
		log.info("Cancelling schedule: scheduleId={}", scheduleIdStr);

		// Step 1: Key로 도메인 조회
		ScheduleId scheduleId = ScheduleId.of(scheduleIdStr);
		ScheduleRule rule = findScheduleRule(scheduleId);

		// Step 2: 도메인 로직 실행
		rule.cancel();

		// Step 3: 영속화
		scheduleRuleRepository.save(rule);

		// Step 4: Quartz Job 삭제
		deleteQuartzJob(scheduleIdStr);
	}

	/**
	 * 사용자의 스케줄 목록 조회
	 */
	public List<ScheduleResponse> getMySchedules() {
		UserId senderId = getUserIdFromContext();

		List<ScheduleRule> rules = scheduleRuleRepository
				.findActiveBySenderId(senderId.getValue());

		return rules.stream()
				.map(ScheduleResponse::from)
				.collect(Collectors.toList());
	}

	/**
	 * 채널의 스케줄 목록 조회
	 */
	public List<ScheduleResponse> getSchedulesByChannel(String channelId) {
		List<ScheduleRule> rules = scheduleRuleRepository
				.findActiveByChannelId(channelId);

		return rules.stream()
				.map(ScheduleResponse::from)
				.collect(Collectors.toList());
	}

	// ========== Private Helper Methods ==========

	/**
	 * 인증된 사용자 ID 조회 (common-auth 컨텍스트 → domain UserId로 변환)
	 */
	private UserId getUserIdFromContext() {
		String userIdStr = SecurityUtils.getCurrentUserId()
				.orElseThrow(() -> new IllegalStateException("User not authenticated"));

		return UserId.of(userIdStr);
	}

	/**
	 * Channel Aggregate 조회
	 */
	private com.example.chat.domain.channel.Channel findChannelById(ChannelId channelId) {
		return channelRepository.findById(channelId)
				.orElseThrow(() -> new IllegalArgumentException(
						"Channel not found: " + channelId.getValue()));
	}

	/**
	 * User Aggregate 조회
	 */
	private com.example.chat.domain.user.User findUserById(UserId userId) {
		return userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException(
						"User not found: " + userId.getValue()));
	}

	/**
	 * Message 도메인 생성 (MessageType에 따라)
	 *
	 * Domain Service에 Aggregate 전달
	 */
	private Message createMessageByType(com.example.chat.domain.channel.Channel channel,
			com.example.chat.domain.user.User sender,
			com.example.chat.domain.message.MessageType messageType,
			java.util.Map<String, Object> payload) {
		// Early Return: MessageType 검증
		if (messageType == null) {
			throw new IllegalArgumentException("Message type is required");
		}

		switch (messageType) {
			case TEXT:
				String text = extractTextField(payload, "text");
				return messageDomainService.createTextMessage(channel, sender, text);

			case IMAGE:
				String imageUrl = extractTextField(payload, "imageUrl");
				String imageName = extractTextFieldOrDefault(payload, "fileName", "image.jpg");
				Long imageSize = extractLongFieldOrDefault(payload, "fileSize", 0L);
				return messageDomainService.createImageMessage(channel, sender, imageUrl, imageName, imageSize);

			case FILE:
				String fileUrl = extractTextField(payload, "fileUrl");
				String fileName = extractTextField(payload, "fileName");
				Long fileSize = extractLongFieldOrDefault(payload, "fileSize", 0L);
				String mimeType = extractTextFieldOrDefault(payload, "mimeType", "application/octet-stream");
				return messageDomainService.createFileMessage(channel, sender, fileUrl, fileName, fileSize, mimeType);

			case SYSTEM:
				String systemText = extractTextField(payload, "text");
				return messageDomainService.createSystemMessage(channel, systemText);

			default:
				throw new IllegalArgumentException("Unsupported message type: " + messageType);
		}
	}

	/**
	 * Payload에서 필수 텍스트 필드 추출
	 */
	private String extractTextField(java.util.Map<String, Object> payload, String fieldName) {
		if (payload == null) {
			throw new IllegalArgumentException("Payload is required");
		}

		Object value = payload.get(fieldName);
		if (value == null) {
			throw new IllegalArgumentException(String.format("Field '%s' is required in payload", fieldName));
		}

		return value.toString();
	}

	/**
	 * Payload에서 텍스트 필드 추출 (기본값 있음)
	 */
	private String extractTextFieldOrDefault(java.util.Map<String, Object> payload, String fieldName,
			String defaultValue) {
		if (payload == null) {
			return defaultValue;
		}

		Object value = payload.get(fieldName);
		return value != null ? value.toString() : defaultValue;
	}

	/**
	 * Payload에서 Long 필드 추출 (기본값 있음)
	 */
	private Long extractLongFieldOrDefault(java.util.Map<String, Object> payload, String fieldName, Long defaultValue) {
		if (payload == null) {
			return defaultValue;
		}

		Object value = payload.get(fieldName);
		if (value == null) {
			return defaultValue;
		}

		if (value instanceof Number) {
			return ((Number) value).longValue();
		}

		try {
			return Long.parseLong(value.toString());
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	/**
	 * Key로 ScheduleRule 조회
	 */
	private ScheduleRule findScheduleRule(ScheduleId scheduleId) {
		return scheduleRuleRepository.findById(scheduleId)
				.orElseThrow(() -> new IllegalArgumentException(
						"Schedule not found: " + scheduleId.getValue()));
	}

	/**
	 * Quartz Job 등록
	 */
	private void registerQuartzJob(ScheduleRule rule) {
		try {
			String scheduleIdStr = rule.getId().getValue();

			JobDetail jobDetail = JobBuilder
					.newJob()
					.ofType(com.example.chat.system.job.MessagePublishJob.class)
					.withIdentity("schedule-" + scheduleIdStr)
					.usingJobData("scheduleId", scheduleIdStr)
					.storeDurably()
					.build();

			Trigger trigger = createTrigger(rule);

			quartzScheduler.scheduleJob(jobDetail, trigger);

			log.info("Quartz job registered: scheduleId={}", scheduleIdStr);

		} catch (SchedulerException e) {
			log.error("Failed to register Quartz job: scheduleId={}",
					rule.getId().getValue(), e);
			throw new RuntimeException("Failed to register schedule", e);
		}
	}

	/**
	 * Trigger 생성
	 */
	private Trigger createTrigger(ScheduleRule rule) {
		String scheduleIdStr = rule.getId().getValue();
		TriggerBuilder<Trigger> builder = TriggerBuilder.newTrigger()
				.withIdentity("trigger-" + scheduleIdStr);

		if (rule.isOneTime()) {
			// 단발성: SimpleSchedule
			Date startTime = Date.from(rule.getScheduledAt());
			builder.startAt(startTime);

		} else {
			// 주기적: CronSchedule
			builder.withSchedule(
					CronScheduleBuilder.cronSchedule(rule.getCronExpression().getValue()));
		}

		return builder.build();
	}

	/**
	 * Quartz Job 삭제
	 */
	private void deleteQuartzJob(String scheduleId) {
		try {
			JobKey jobKey = new JobKey("schedule-" + scheduleId);
			quartzScheduler.deleteJob(jobKey);

			log.info("Quartz job deleted: scheduleId={}", scheduleId);

		} catch (SchedulerException e) {
			log.error("Failed to delete Quartz job: scheduleId={}", scheduleId, e);
		}
	}
}
