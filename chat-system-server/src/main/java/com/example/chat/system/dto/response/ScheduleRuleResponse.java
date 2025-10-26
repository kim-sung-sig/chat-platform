package com.example.chat.system.dto.response;

import com.example.chat.system.domain.entity.ScheduleRule;
import com.example.chat.system.domain.enums.ScheduleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 스케줄 규칙 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleRuleResponse {

    private Long id;
    private Long messageId;
    private String messageTitle;
    private ScheduleType scheduleType;
    private String cronExpression;
    private LocalDateTime executionTime;
    private LocalDateTime nextExecutionTime;
    private LocalDateTime lastExecutionTime;
    private Boolean isActive;
    private Integer executionCount;
    private Integer maxExecutionCount;
    private LocalDateTime createdAt;

    /**
     * Entity -> DTO 변환
     */
    public static ScheduleRuleResponse from(ScheduleRule scheduleRule) {
        return ScheduleRuleResponse.builder()
                .id(scheduleRule.getId())
                .messageId(scheduleRule.getMessage().getId())
                .messageTitle(scheduleRule.getMessage().getTitle())
                .scheduleType(scheduleRule.getScheduleType())
                .cronExpression(scheduleRule.getCronExpression())
                .executionTime(scheduleRule.getExecutionTime())
                .nextExecutionTime(scheduleRule.getNextExecutionTime())
                .lastExecutionTime(scheduleRule.getLastExecutionTime())
                .isActive(scheduleRule.getIsActive())
                .executionCount(scheduleRule.getExecutionCount())
                .maxExecutionCount(scheduleRule.getMaxExecutionCount())
                .createdAt(scheduleRule.getCreatedAt())
                .build();
    }
}