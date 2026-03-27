package com.example.chat.scheduled.domain.model;

import com.example.chat.message.domain.MessageContent;

import java.time.ZonedDateTime;

/**
 * 예약 발송 메시지 — Aggregate Root (순수 도메인 POJO)
 *
 * 불변식:
 * - scheduledAt > now() + 5분
 * - scheduledAt < now() + 30일
 * - 취소는 PENDING 상태에서만 허용
 * - retryCount <= MAX_RETRY (3)
 */
public class ScheduledMessage {

    public static final int MAX_RETRY = 3;

    private final String id;
    private final String channelId;
    private final String senderId;
    private final MessageContent content;
    private final ScheduleType scheduleType;
    private ScheduleStatus status;
    private final ZonedDateTime scheduledAt;
    private final ZonedDateTime createdAt;
    private ZonedDateTime executedAt;
    private ZonedDateTime cancelledAt;
    private int retryCount;

    public ScheduledMessage(
            String id,
            String channelId,
            String senderId,
            MessageContent content,
            ScheduleType scheduleType,
            ScheduleStatus status,
            ZonedDateTime scheduledAt,
            ZonedDateTime createdAt,
            ZonedDateTime executedAt,
            ZonedDateTime cancelledAt,
            int retryCount) {
        this.id = id;
        this.channelId = channelId;
        this.senderId = senderId;
        this.content = content;
        this.scheduleType = scheduleType;
        this.status = status;
        this.scheduledAt = scheduledAt;
        this.createdAt = createdAt;
        this.executedAt = executedAt;
        this.cancelledAt = cancelledAt;
        this.retryCount = retryCount;
    }

    // ── 도메인 행위 ──────────────────────────────────────────────────

    /**
     * 예약 취소. PENDING 상태에서만 허용.
     *
     * @throws IllegalStateException 이미 실행됐거나 취소된 경우
     */
    public void cancel() {
        if (this.status != ScheduleStatus.PENDING) {
            throw new IllegalStateException("PENDING 상태에서만 취소할 수 있습니다. 현재 상태: " + this.status);
        }
        this.status = ScheduleStatus.CANCELLED;
        this.cancelledAt = ZonedDateTime.now();
    }

    /** Job 실행 시작 — PENDING → EXECUTING */
    public void markExecuting() {
        if (this.status != ScheduleStatus.PENDING) {
            throw new IllegalStateException("PENDING 상태에서만 실행 시작 가능. 현재 상태: " + this.status);
        }
        this.status = ScheduleStatus.EXECUTING;
    }

    /** 발송 완료 — EXECUTING → EXECUTED */
    public void markExecuted() {
        if (this.status != ScheduleStatus.EXECUTING) {
            throw new IllegalStateException("EXECUTING 상태에서만 완료 전이 가능. 현재 상태: " + this.status);
        }
        this.status = ScheduleStatus.EXECUTED;
        this.executedAt = ZonedDateTime.now();
    }

    /**
     * 발송 실패 처리. retryCount 를 증가시킨다.
     * 최대 재시도 초과 시 FAILED 로 전이.
     */
    public void markFailed() {
        this.retryCount++;
        if (this.retryCount >= MAX_RETRY) {
            this.status = ScheduleStatus.FAILED;
        } else {
            this.status = ScheduleStatus.PENDING;
        }
    }

    /** 재시도 가능 여부 */
    public boolean isRetryable() {
        return this.retryCount < MAX_RETRY;
    }

    // ── Getters ──────────────────────────────────────────────────────

    public String getId()               { return id; }
    public String getChannelId()        { return channelId; }
    public String getSenderId()         { return senderId; }
    public MessageContent getContent()  { return content; }
    public ScheduleType getScheduleType() { return scheduleType; }
    public ScheduleStatus getStatus()   { return status; }
    public ZonedDateTime getScheduledAt() { return scheduledAt; }
    public ZonedDateTime getCreatedAt() { return createdAt; }
    public ZonedDateTime getExecutedAt() { return executedAt; }
    public ZonedDateTime getCancelledAt() { return cancelledAt; }
    public int getRetryCount()          { return retryCount; }
}
