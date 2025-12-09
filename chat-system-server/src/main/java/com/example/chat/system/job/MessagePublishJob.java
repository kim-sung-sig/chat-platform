package com.example.chat.system.job;

import com.example.chat.domain.schedule.ScheduleRule;
import com.example.chat.domain.schedule.ScheduleRuleRepository;
import com.example.chat.system.infrastructure.lock.DistributedLockService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

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
	private final RestTemplate restTemplate;
	private final ObjectMapper objectMapper;

	@Override
	public void execute(JobExecutionContext context) {
		JobDataMap dataMap = context.getJobDetail().getJobDataMap();
		Long scheduleId = dataMap.getLong("scheduleId");

		log.info("MessagePublishJob started: scheduleId={}", scheduleId);

		// Step 1: 분산 락 획득 시도
		if (!lockService.tryLock(scheduleId)) {
			log.info("Another instance is processing: scheduleId={}", scheduleId);
			return;
		}

		try {
			// Step 2: ScheduleRule 조회 (락 획득 후)
			ScheduleRule rule = findScheduleRule(scheduleId);

			// Early return: 스케줄이 없거나 실행 불가능
			if (rule == null) {
				log.warn("Schedule not found: scheduleId={}", scheduleId);
				return;
			}

			if (!rule.getStatus().isExecutable()) {
				log.info("Schedule not executable: scheduleId={}, status={}",
						scheduleId, rule.getStatus());
				return;
			}

			// Step 3: 메시지 발송
			publishMessage(rule);

			// Step 4: 실행 횟수 증가 및 상태 업데이트
			ScheduleRule updatedRule = rule.execute();
			scheduleRuleRepository.save(updatedRule);

			log.info("MessagePublishJob completed: scheduleId={}, executionCount={}/{}",
					scheduleId,
					updatedRule.getExecutionCount(),
					updatedRule.getMaxExecutionCount());

		} catch (Exception e) {
			log.error("MessagePublishJob failed: scheduleId={}", scheduleId, e);
			throw new RuntimeException("Failed to publish scheduled message", e);

		} finally {
			// Step 5: 락 해제
			lockService.unlock(scheduleId);
		}
	}

	/**
	 * ScheduleRule 조회
	 */
	private ScheduleRule findScheduleRule(Long scheduleId) {
		return scheduleRuleRepository.findById(scheduleId).orElse(null);
	}

	/**
	 * 메시지 발송 (chat-message-server API 호출)
	 */
	private void publishMessage(ScheduleRule rule) {
		try {
			// Payload JSON 파싱
			Map<String, Object> payload = parsePayload(rule.getMessagePayloadJson());

			// 요청 DTO 생성
			Map<String, Object> request = new HashMap<>();
			request.put("roomId", rule.getRoomId());
			request.put("channelId", rule.getChannelId());
			request.put("messageType", rule.getMessageType().getCode());
			request.put("payload", payload);

			// HTTP 헤더 설정
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			// TODO: 실제 인증 토큰 추가

			HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

			// chat-message-server API 호출
			String url = "http://localhost:8081/api/messages";  // TODO: 설정으로 분리
			restTemplate.postForObject(url, entity, Map.class);

			log.info("Message published: scheduleId={}, roomId={}",
					rule.getScheduleId(), rule.getRoomId());

		} catch (Exception e) {
			log.error("Failed to publish message: scheduleId={}",
					rule.getScheduleId(), e);
			throw new RuntimeException("Message publish failed", e);
		}
	}

	/**
	 * JSON Payload 파싱
	 */
	private Map<String, Object> parsePayload(String payloadJson) {
		try {
			return objectMapper.readValue(
					payloadJson,
					new TypeReference<Map<String, Object>>() {}
			);
		} catch (Exception e) {
			log.error("Failed to parse payload: {}", payloadJson, e);
			return new HashMap<>();
		}
	}
}
