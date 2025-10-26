package com.example.chat.system.dto.request;

import com.example.chat.system.domain.enums.ScheduleType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 스케줄 생성 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleCreateRequest {

    @NotNull(message = "메시지 ID는 필수입니다")
    private Long messageId;

    @NotNull(message = "스케줄 타입은 필수입니다")
    private ScheduleType scheduleType;

    private String cronExpression; // RECURRING일 때 필수

    private LocalDateTime executionTime; // ONCE일 때 필수

    private Integer maxExecutionCount; // 최대 실행 횟수 (옵션)
}