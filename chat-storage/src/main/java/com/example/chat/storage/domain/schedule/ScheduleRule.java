package com.example.chat.storage.domain.schedule;

import com.example.chat.common.auth.model.UserId;
import com.example.chat.storage.domain.message.MessageType;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 스케줄 규칙 Aggregate Root
 * 예약 메시지의 실행 규칙을 정의
 */
@Getter
@Builder(toBuilder = true)
public class ScheduleRule {

    private Long scheduleId;
    private String roomId;
    private String channelId;
    private UserId senderId;

    private ScheduleType type;
    private ScheduleStatus status;

    // 단발성 스케줄
    private LocalDateTime executeAt;

    // 주기적 스케줄
    private String cronExpression;

    // 실행 횟수 관리
    private Integer maxExecutionCount;  // null = 무제한
    private Integer executionCount;

    // 메시지 정보
    private MessageType messageType;
    private String messagePayloadJson;  // JSON 직렬화된 payload

    // 메타데이터
    private Instant createdAt;
    private Instant updatedAt;
    private Long version;  // 낙관적 락

    /**
     * 단발성 스케줄 생성 (팩토리 메서드)
     */
    public static ScheduleRule createOneTime(
            String roomId,
            String channelId,
            UserId senderId,
            MessageType messageType,
            Map<String, Object> payload,
            LocalDateTime executeAt
    ) {
        // Early return: executeAt 검증
        if (executeAt == null) {
            throw new IllegalArgumentException("executeAt cannot be null for one-time schedule");
        }

        // Early return: 과거 시간 검증
        if (executeAt.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("executeAt must be in the future");
        }

        return ScheduleRule.builder()
                .roomId(roomId)
                .channelId(channelId)
                .senderId(senderId)
                .type(ScheduleType.ONE_TIME)
                .status(ScheduleStatus.ACTIVE)
                .executeAt(executeAt)
                .messageType(messageType)
                .messagePayloadJson(serializePayload(payload))
                .executionCount(0)
                .maxExecutionCount(1)  // 단발성은 1회만
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .version(0L)
                .build();
    }

    /**
     * 주기적 스케줄 생성 (팩토리 메서드)
     */
    public static ScheduleRule createRecurring(
            String roomId,
            String channelId,
            UserId senderId,
            MessageType messageType,
            Map<String, Object> payload,
            String cronExpression,
            Integer maxExecutionCount
    ) {
        // Early return: cronExpression 검증
        if (cronExpression == null || cronExpression.trim().isEmpty()) {
            throw new IllegalArgumentException("cronExpression cannot be null or empty");
        }

        // Early return: cron 유효성 검증 (간단한 검증)
        validateCronExpression(cronExpression);

        return ScheduleRule.builder()
                .roomId(roomId)
                .channelId(channelId)
                .senderId(senderId)
                .type(ScheduleType.RECURRING)
                .status(ScheduleStatus.ACTIVE)
                .cronExpression(cronExpression)
                .messageType(messageType)
                .messagePayloadJson(serializePayload(payload))
                .executionCount(0)
                .maxExecutionCount(maxExecutionCount)  // null 가능 (무제한)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .version(0L)
                .build();
    }

    /**
     * 스케줄 실행 (도메인 로직)
     */
    public ScheduleRule execute() {
        // Early return: 실행 불가능한 상태
        if (!status.isExecutable()) {
            throw new IllegalStateException(
                String.format("Cannot execute schedule in status: %s", status)
            );
        }

        // 실행 횟수 증가
        Integer newExecutionCount = this.executionCount + 1;

        // 완료 조건 체크
        ScheduleStatus newStatus = this.status;
        if (shouldComplete(newExecutionCount)) {
            newStatus = ScheduleStatus.COMPLETED;
        }

        return this.toBuilder()
                .executionCount(newExecutionCount)
                .status(newStatus)
                .updatedAt(Instant.now())
                .version(this.version + 1)
                .build();
    }

    /**
     * 일시중지
     */
    public ScheduleRule pause() {
        // Early return: 활성 상태가 아니면 중지 불가
        if (this.status != ScheduleStatus.ACTIVE) {
            throw new IllegalStateException("Can only pause ACTIVE schedule");
        }

        return this.toBuilder()
                .status(ScheduleStatus.PAUSED)
                .updatedAt(Instant.now())
                .version(this.version + 1)
                .build();
    }

    /**
     * 재개
     */
    public ScheduleRule resume() {
        // Early return: 일시중지 상태가 아니면 재개 불가
        if (this.status != ScheduleStatus.PAUSED) {
            throw new IllegalStateException("Can only resume PAUSED schedule");
        }

        return this.toBuilder()
                .status(ScheduleStatus.ACTIVE)
                .updatedAt(Instant.now())
                .version(this.version + 1)
                .build();
    }

    /**
     * 취소
     */
    public ScheduleRule cancel() {
        // Early return: 이미 완료되었거나 취소된 경우
        if (this.status == ScheduleStatus.COMPLETED ||
            this.status == ScheduleStatus.CANCELLED) {
            throw new IllegalStateException("Cannot cancel completed or cancelled schedule");
        }

        return this.toBuilder()
                .status(ScheduleStatus.CANCELLED)
                .updatedAt(Instant.now())
                .version(this.version + 1)
                .build();
    }

    /**
     * 완료 조건 체크
     */
    private boolean shouldComplete(Integer executionCount) {
        if (this.type == ScheduleType.ONE_TIME) {
            return true;  // 단발성은 1회 실행 후 완료
        }

        if (this.maxExecutionCount == null) {
            return false;  // 무제한
        }

        return executionCount >= this.maxExecutionCount;
    }

    /**
     * Payload 직렬화 (JSON)
     */
    private static String serializePayload(Map<String, Object> payload) {
        // 실제로는 ObjectMapper 사용
        // 여기서는 간단히 toString
        return payload != null ? payload.toString() : "{}";
    }

    /**
     * Cron 표현식 유효성 검증 (간단한 검증)
     */
    private static void validateCronExpression(String cronExpression) {
        String[] parts = cronExpression.split(" ");
        if (parts.length < 5 || parts.length > 7) {
            throw new IllegalArgumentException("Invalid cron expression format");
        }
    }

    /**
     * 도메인 검증
     */
    public void validate() {
        if (roomId == null || roomId.trim().isEmpty()) {
            throw new IllegalArgumentException("roomId is required");
        }

        if (senderId == null) {
            throw new IllegalArgumentException("senderId is required");
        }

        if (messageType == null) {
            throw new IllegalArgumentException("messageType is required");
        }

        if (type == ScheduleType.ONE_TIME && executeAt == null) {
            throw new IllegalArgumentException("executeAt is required for ONE_TIME schedule");
        }

        if (type == ScheduleType.RECURRING &&
            (cronExpression == null || cronExpression.trim().isEmpty())) {
            throw new IllegalArgumentException("cronExpression is required for RECURRING schedule");
        }
    }
}
