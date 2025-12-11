package com.example.chat.system.dto.response;

import com.example.chat.domain.schedule.ScheduleRule;
import com.example.chat.domain.schedule.ScheduleStatus;
import com.example.chat.domain.schedule.ScheduleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 스케줄 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleResponse {

	private String scheduleId;
	private String channelId;
	private String senderId;

	private ScheduleType type;
	private ScheduleStatus status;

	private LocalDateTime executeAt;       // 단발성
	private String cronExpression;         // 주기적

	private String messageType;
	private String messageContent;

	private Instant createdAt;
	private Instant executedAt;
	private Instant cancelledAt;

	/**
	 * ScheduleRule 도메인을 DTO로 변환
	 */
	public static ScheduleResponse from(ScheduleRule rule) {
		return ScheduleResponse.builder()
				.scheduleId(rule.getId().getValue())
				.channelId(rule.getMessage().getChannelId().getValue())
				.senderId(rule.getMessage().getSenderId().getValue())
				.type(rule.getType())
				.status(rule.getStatus())
				.executeAt(rule.isOneTime() && rule.getScheduledAt() != null
						? LocalDateTime.ofInstant(rule.getScheduledAt(), ZoneId.systemDefault())
						: null)
				.cronExpression(rule.isRecurring() && rule.getCronExpression() != null
						? rule.getCronExpression().getValue()
						: null)
				.messageType(rule.getMessage().getType().name())
				.messageContent(rule.getMessage().getContent().getText())
				.createdAt(rule.getCreatedAt())
				.executedAt(rule.getExecutedAt())
				.cancelledAt(rule.getCancelledAt())
				.build();
	}
}
