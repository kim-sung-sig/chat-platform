package com.example.chat.system.dto.request;

import com.example.chat.storage.domain.message.MessageType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

/**
 * 주기적 스케줄 생성 요청 DTO
 */
@Getter
@Builder
public class CreateRecurringScheduleRequest {

    @NotBlank(message = "roomId is required")
    private String roomId;

    private String channelId;

    @NotNull(message = "messageType is required")
    private MessageType messageType;

    @NotNull(message = "payload is required")
    private Map<String, Object> payload;

    @NotBlank(message = "cronExpression is required")
    @Pattern(
        regexp = "^([0-9*,/-]+\\s+){5,6}[0-9*,/-]+$",
        message = "Invalid cron expression format"
    )
    private String cronExpression;

    @Min(value = 1, message = "maxExecutionCount must be at least 1")
    private Integer maxExecutionCount;  // null이면 무제한
}
