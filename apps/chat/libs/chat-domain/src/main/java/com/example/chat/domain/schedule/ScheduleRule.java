package com.example.chat.domain.schedule;

import com.example.chat.domain.message.Message;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * 스케줄 규칙 Aggregate Root
 */
@Getter
@Builder
public class ScheduleRule {
    private final ScheduleId id;
    private final ScheduleType type;
    private ScheduleStatus status;
    private final Message message;
    private final CronExpression cronExpression;  // RECURRING일 때만 사용
    private final Instant scheduledAt;            // ONE_TIME일 때만 사용
    private final Instant createdAt;
    private Instant executedAt;
    private Instant cancelledAt;

    /**
     * 단발성 스케줄 생성
     */
    public static ScheduleRule oneTime(Message message, Instant scheduledAt) {
        if (scheduledAt.isBefore(Instant.now())) {
            throw new IllegalArgumentException("Scheduled time must be in the future");
        }

        return ScheduleRule.builder()
                .id(ScheduleId.generate())
                .type(ScheduleType.ONE_TIME)
                .status(ScheduleStatus.PENDING)
                .message(message)
                .scheduledAt(scheduledAt)
                .createdAt(Instant.now())
                .build();
    }

    /**
     * 주기적 스케줄 생성
     */
    public static ScheduleRule recurring(Message message, CronExpression cronExpression) {
        return ScheduleRule.builder()
                .id(ScheduleId.generate())
                .type(ScheduleType.RECURRING)
                .status(ScheduleStatus.ACTIVE)
                .message(message)
                .cronExpression(cronExpression)
                .createdAt(Instant.now())
                .build();
    }

    /**
     * 스케줄 취소
     */
    public void cancel() {
        if (this.status == ScheduleStatus.EXECUTED) {
            throw new IllegalStateException("Cannot cancel already executed schedule");
        }
        if (this.status == ScheduleStatus.CANCELLED) {
            throw new IllegalStateException("Schedule is already cancelled");
        }

        this.status = ScheduleStatus.CANCELLED;
        this.cancelledAt = Instant.now();
    }

    /**
     * 실행 완료 표시 (단발성만)
     */
    public void markAsExecuted() {
        if (this.type != ScheduleType.ONE_TIME) {
            throw new IllegalStateException("Only ONE_TIME schedules can be marked as executed");
        }
        if (this.status != ScheduleStatus.PENDING && this.status != ScheduleStatus.ACTIVE) {
            throw new IllegalStateException("Schedule must be in PENDING or ACTIVE status to execute");
        }

        this.status = ScheduleStatus.EXECUTED;
        this.executedAt = Instant.now();
    }

    /**
     * 실패 표시
     */
    public void markAsFailed() {
        this.status = ScheduleStatus.FAILED;
    }

    /**
     * 실행 가능한 상태인지 확인
     */
    public boolean canBeExecuted() {
        return this.status == ScheduleStatus.PENDING || this.status == ScheduleStatus.ACTIVE;
    }

    /**
     * 단발성 스케줄인지 확인
     */
    public boolean isOneTime() {
        return this.type == ScheduleType.ONE_TIME;
    }

    /**
     * 주기적 스케줄인지 확인
     */
    public boolean isRecurring() {
        return this.type == ScheduleType.RECURRING;
    }
}
