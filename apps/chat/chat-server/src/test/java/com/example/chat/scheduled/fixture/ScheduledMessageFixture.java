package com.example.chat.scheduled.fixture;

import com.example.chat.message.domain.MessageContent;
import com.example.chat.scheduled.domain.model.ScheduleStatus;
import com.example.chat.scheduled.domain.model.ScheduleType;
import com.example.chat.scheduled.domain.model.ScheduledMessage;
import com.example.chat.scheduled.rest.dto.request.CreateScheduledMessageRequest;

import java.time.ZonedDateTime;

/**
 * ScheduledMessage 테스트 픽스처
 *
 * 반복적인 도메인 객체 생성 코드를 중앙화합니다.
 * 모든 테스트 클래스에서 이 클래스를 통해 표준 테스트 데이터를 생성하세요.
 */
public class ScheduledMessageFixture {

    public static final String SCHEDULE_ID = "sched-001";
    public static final String CHANNEL_ID  = "ch-001";
    public static final String SENDER_ID   = "user-001";
    public static final ZonedDateTime BASE_NOW = ZonedDateTime.now();

    // ── 도메인 객체 팩토리 ────────────────────────────────────────────

    /** PENDING 상태 기본 예약 메시지 */
    public static ScheduledMessage pending() {
        return new ScheduledMessage(
                SCHEDULE_ID, CHANNEL_ID, SENDER_ID,
                MessageContent.text("테스트 예약 메시지"),
                ScheduleType.ONCE,
                ScheduleStatus.PENDING,
                BASE_NOW.plusHours(1),
                BASE_NOW,
                null, null,
                0
        );
    }

    /** PENDING, 지정 retryCount */
    public static ScheduledMessage pendingWithRetry(int retryCount) {
        return new ScheduledMessage(
                SCHEDULE_ID, CHANNEL_ID, SENDER_ID,
                MessageContent.text("retry msg"),
                ScheduleType.ONCE,
                ScheduleStatus.PENDING,
                BASE_NOW.plusHours(1),
                BASE_NOW,
                null, null,
                retryCount
        );
    }

    /** EXECUTING 상태 */
    public static ScheduledMessage executing() {
        var msg = pending();
        msg.markExecuting();
        return msg;
    }

    /** EXECUTED 상태 */
    public static ScheduledMessage executed() {
        var msg = executing();
        msg.markExecuted();
        return msg;
    }

    /** CANCELLED 상태 */
    public static ScheduledMessage cancelled() {
        var msg = pending();
        msg.cancel();
        return msg;
    }

    /** 다른 사용자 소유 예약 */
    public static ScheduledMessage pendingByOtherUser() {
        return new ScheduledMessage(
                SCHEDULE_ID, CHANNEL_ID, "other-user",
                MessageContent.text("다른 사용자 메시지"),
                ScheduleType.ONCE,
                ScheduleStatus.PENDING,
                BASE_NOW.plusHours(1),
                BASE_NOW,
                null, null,
                0
        );
    }

    /** IMAGE 타입 예약 */
    public static ScheduledMessage pendingImage() {
        return new ScheduledMessage(
                SCHEDULE_ID, CHANNEL_ID, SENDER_ID,
                MessageContent.image("https://cdn.example.com/img.png", "img.png", 1024L),
                ScheduleType.ONCE,
                ScheduleStatus.PENDING,
                BASE_NOW.plusHours(1),
                BASE_NOW,
                null, null,
                0
        );
    }

    // ── HTTP 요청 DTO 팩토리 ──────────────────────────────────────────

    public static CreateScheduledMessageRequest textRequest(ZonedDateTime scheduledAt) {
        return new CreateScheduledMessageRequest(
                CHANNEL_ID, "TEXT", "예약 메시지", null, null, null, null, scheduledAt);
    }

    public static CreateScheduledMessageRequest imageRequest(ZonedDateTime scheduledAt) {
        return new CreateScheduledMessageRequest(
                CHANNEL_ID, "IMAGE", null,
                "https://cdn.example.com/img.png", "img.png", 1024L, null,
                scheduledAt);
    }
}
