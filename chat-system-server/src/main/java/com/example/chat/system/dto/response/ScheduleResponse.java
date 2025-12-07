package com.example.chat.system.dto.response;

import com.example.chat.storage.domain.schedule.ScheduleRule;
import com.example.chat.storage.domain.schedule.ScheduleStatus;
import com.example.chat.storage.domain.schedule.ScheduleType;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * 스케줄 응답 DTO
 */
@Getter
@Builder
public class ScheduleResponse {

    private Long scheduleId;
    private String roomId;
    private String channelId;
    private Long senderId;

    private ScheduleType type;
    private ScheduleStatus status;

    private LocalDateTime executeAt;       // 단발성
    private String cronExpression;         // 주기적

    private Integer executionCount;
    private Integer maxExecutionCount;

    private String messageType;
    private String messagePayloadJson;

    private Instant createdAt;
    private Instant updatedAt;

    /**
     * ScheduleRule 도메인을 DTO로 변환
     */
    public static ScheduleResponse from(ScheduleRule rule) {
        return ScheduleResponse.builder()
                .scheduleId(rule.getScheduleId())
                .roomId(rule.getRoomId())
                .channelId(rule.getChannelId())
                .senderId(rule.getSenderId().getValue())
                .type(rule.getType())
                .status(rule.getStatus())
                .executeAt(rule.getExecuteAt())
                .cronExpression(rule.getCronExpression())
                .executionCount(rule.getExecutionCount())
                .maxExecutionCount(rule.getMaxExecutionCount())
                .messageType(rule.getMessageType().getCode())
                .messagePayloadJson(rule.getMessagePayloadJson())
                .createdAt(rule.getCreatedAt())
                .updatedAt(rule.getUpdatedAt())
                .build();
    }
}
