package com.example.chat.system.job;

import com.example.chat.domain.schedule.ScheduleId;
import com.example.chat.domain.schedule.ScheduleRule;
import com.example.chat.domain.schedule.ScheduleRuleRepository;
import com.example.chat.system.infrastructure.lock.DistributedLockService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

/**
 * 예약 메시지 발행 Job
 * Quartz Scheduler에 의해 실행됨
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessagePublishJob implements Job {

	private final ScheduleRuleRepository scheduleRuleRepository;
	private final DistributedLockService lockService;
	private final WebClient webClient;
	private final ObjectMapper objectMapper;

	@Override
	public void execute(JobExecutionContext context) {
		JobDataMap dataMap = context.getJobDetail().getJobDataMap();
		String scheduleIdStr = dataMap.getString("scheduleId");

		log.info("MessagePublishJob started: scheduleId={}", scheduleIdStr);

		// Step 1: 분산 락 획득 시도
		if (!lockService.tryLock(scheduleIdStr)) {
			log.info("Another instance is processing: scheduleId={}", scheduleIdStr);
			return;
		}

		try {
			// Step 2: ScheduleRule 조회 (락 획득 후)
			ScheduleId scheduleId = ScheduleId.of(scheduleIdStr);
			ScheduleRule rule = findScheduleRule(scheduleId);

			// Early return: 스케줄이 없거나 실행 불가능
			if (rule == null) {
				log.warn("Schedule not found: scheduleId={}", scheduleIdStr);
				return;
			}

			if (!rule.canBeExecuted()) {
				log.info("Schedule not executable: scheduleId={}, status={}",
						scheduleIdStr, rule.getStatus());
				return;
			}

			// Step 3: 메시지 발송
			publishMessage(rule);

			// Step 4: 단발성이면 실행 완료 표시
			if (rule.isOneTime()) {
				rule.markAsExecuted();
				scheduleRuleRepository.save(rule);
			}

			log.info("MessagePublishJob completed: scheduleId={}", scheduleIdStr);

		} catch (Exception e) {
			log.error("MessagePublishJob failed: scheduleId={}", scheduleIdStr, e);
			// 실패 시 상태 업데이트
			try {
				ScheduleId scheduleId = ScheduleId.of(scheduleIdStr);
				ScheduleRule rule = findScheduleRule(scheduleId);
				if (rule != null) {
					rule.markAsFailed();
					scheduleRuleRepository.save(rule);
				}
			} catch (Exception ex) {
				log.error("Failed to mark schedule as failed: scheduleId={}", scheduleIdStr, ex);
			}
			throw new RuntimeException("Failed to publish scheduled message", e);

		} finally {
			// Step 5: 락 해제
			lockService.unlock(scheduleIdStr);
		}
	}

	/**
	 * ScheduleRule 조회
	 */
	private ScheduleRule findScheduleRule(ScheduleId scheduleId) {
		return scheduleRuleRepository.findById(scheduleId).orElse(null);
	}

	/**
	 * 메시지 발송 (chat-message-server API 호출)
	 * WebClient 사용 - 비동기, 논블로킹, Connection Pool 관리
	 */
	private void publishMessage(ScheduleRule rule) {
		try {
			// Payload 생성 (MessageContent로부터)
			Map<String, Object> payload = new HashMap<>();
			payload.put("text", rule.getMessage().getContent().getText());

			if (rule.getMessage().getContent().getMediaUrl() != null) {
				payload.put("mediaUrl", rule.getMessage().getContent().getMediaUrl());
				payload.put("fileName", rule.getMessage().getContent().getFileName());
				payload.put("fileSize", rule.getMessage().getContent().getFileSize());
			}

			// 요청 DTO 생성
			Map<String, Object> request = new HashMap<>();
			request.put("channelId", rule.getMessage().getChannelId().getValue());
			request.put("messageType", rule.getMessage().getType().name());
			request.put("payload", payload);

			// chat-message-server API 호출 (WebClient - 비동기)
			String url = "http://localhost:8081/api/messages";  // TODO: 설정으로 분리

			webClient.post()
					.uri(url)
					.contentType(MediaType.APPLICATION_JSON)
					// TODO: 실제 인증 토큰 추가 (senderId 사용)
					// .header("Authorization", "Bearer " + token)
					.bodyValue(request)
					.retrieve()
					.bodyToMono(Map.class)
					.block();  // 동기 대기 (Quartz Job은 동기 실행)

			log.info("Message published: scheduleId={}, channelId={}",
					rule.getId().getValue(), rule.getMessage().getChannelId().getValue());

		} catch (Exception e) {
			log.error("Failed to publish message: scheduleId={}",
					rule.getId().getValue(), e);
			throw new RuntimeException("Message publish failed", e);
		}
	}
}
