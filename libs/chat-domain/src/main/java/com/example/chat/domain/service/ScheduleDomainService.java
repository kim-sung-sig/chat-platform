package com.example.chat.domain.service;

import com.example.chat.domain.message.Message;
import com.example.chat.domain.schedule.CronExpression;
import com.example.chat.domain.schedule.ScheduleRule;

import java.time.Instant;

/**
 * 스케줄 도메인 서비스
 *
 * DDD Domain Service의 역할:
 * - ScheduleRule Aggregate 생성 시 복잡한 도메인 규칙 검증
 * - Message + ScheduleRule Aggregate 간 협력 조율
 */
public class ScheduleDomainService {

	/**
	 * 단발성 스케줄 생성
	 *
	 * Domain Rule:
	 * - 스케줄 시간은 현재보다 미래여야 함
	 * - 스케줄 시간은 1년 이내여야 함
	 *
	 * @param message 예약 메시지 (Aggregate Root)
	 * @param scheduledAt 예약 시간
	 * @return 생성된 스케줄 규칙 (Aggregate Root)
	 */
	public ScheduleRule createOneTimeSchedule(Message message, Instant scheduledAt) {
		// Early Return: 입력값 검증
		validateScheduledTime(scheduledAt);

		// ScheduleRule 생성
		return ScheduleRule.oneTime(message, scheduledAt);
	}

	/**
	 * 주기적 스케줄 생성
	 *
	 * Domain Rule:
	 * - Cron Expression이 유효해야 함
	 *
	 * @param message 반복 메시지 템플릿 (Aggregate Root)
	 * @param cronExpression Cron 표현식
	 * @return 생성된 스케줄 규칙 (Aggregate Root)
	 */
	public ScheduleRule createRecurringSchedule(Message message, CronExpression cronExpression) {
		// Early Return: Cron Expression null 체크
		if (cronExpression == null) {
			throw new IllegalArgumentException("Cron expression cannot be null");
		}

		// ScheduleRule 생성
		return ScheduleRule.recurring(message, cronExpression);
	}

	// ============================================================
	// 입력값 검증 메서드
	// ============================================================

	/**
	 * 스케줄 시간 검증
	 */
	private void validateScheduledTime(Instant scheduledAt) {
		// Early Return: null 체크
		if (scheduledAt == null) {
			throw new IllegalArgumentException("Scheduled time cannot be null");
		}

		// Early Return: 과거 시간 체크
		if (scheduledAt.isBefore(Instant.now())) {
			throw new IllegalArgumentException("Scheduled time must be in the future");
		}

		// Early Return: 너무 먼 미래 제한 (1년)
		Instant oneYearLater = Instant.now().plusSeconds(365 * 24 * 60 * 60);
		if (scheduledAt.isAfter(oneYearLater)) {
			throw new IllegalArgumentException("Scheduled time cannot be more than 1 year in the future");
		}
	}
}
