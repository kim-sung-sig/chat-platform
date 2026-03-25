package com.example.chat.scheduled.domain.model;

import com.example.chat.message.domain.MessageContent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * [단위 테스트] ScheduledMessage 도메인 모델
 *
 * 검증 범위: 상태 전이 불변식, 재시도 횟수 관리
 */
@DisplayName("ScheduledMessage 도메인 단위 테스트")
class ScheduledMessageTest {

    // ── Fixture ───────────────────────────────────────────────────────

    private static final String SCHEDULE_ID = "schedule-001";
    private static final String CHANNEL_ID  = "channel-001";
    private static final String SENDER_ID   = "user-001";
    private static final ZonedDateTime NOW  = ZonedDateTime.now();

    private ScheduledMessage pendingMessage() {
        return new ScheduledMessage(
                SCHEDULE_ID, CHANNEL_ID, SENDER_ID,
                MessageContent.text("테스트 예약 메시지"),
                ScheduleType.ONCE,
                ScheduleStatus.PENDING,
                NOW.plusHours(1),
                NOW,
                null, null,
                0
        );
    }

    private ScheduledMessage messageWithRetry(int retryCount) {
        return new ScheduledMessage(
                SCHEDULE_ID, CHANNEL_ID, SENDER_ID,
                MessageContent.text("retry msg"),
                ScheduleType.ONCE,
                ScheduleStatus.PENDING,
                NOW.plusHours(1),
                NOW,
                null, null,
                retryCount
        );
    }

    // ── cancel() ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("예약 취소 (cancel)")
    class Cancel {

        @Test
        @DisplayName("PENDING 상태에서 취소 - CANCELLED 전이 성공")
        void givenPending_whenCancel_thenCancelled() {
            // Given
            ScheduledMessage msg = pendingMessage();

            // When
            msg.cancel();

            // Then
            assertThat(msg.getStatus()).isEqualTo(ScheduleStatus.CANCELLED);
            assertThat(msg.getCancelledAt()).isNotNull();
        }

        @Test
        @DisplayName("EXECUTED 상태에서 취소 - IllegalStateException 발생")
        void givenExecuted_whenCancel_thenException() {
            // Given
            ScheduledMessage msg = pendingMessage();
            msg.markExecuting();
            msg.markExecuted();

            // When / Then
            assertThatThrownBy(msg::cancel)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("PENDING");
        }

        @Test
        @DisplayName("EXECUTING 상태에서 취소 - IllegalStateException 발생")
        void givenExecuting_whenCancel_thenException() {
            // Given
            ScheduledMessage msg = pendingMessage();
            msg.markExecuting();

            // When / Then
            assertThatThrownBy(msg::cancel)
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("CANCELLED 상태에서 재취소 - IllegalStateException 발생")
        void givenCancelled_whenCancelAgain_thenException() {
            // Given
            ScheduledMessage msg = pendingMessage();
            msg.cancel();

            // When / Then
            assertThatThrownBy(msg::cancel)
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    // ── markExecuting() ───────────────────────────────────────────────

    @Nested
    @DisplayName("실행 시작 (markExecuting)")
    class MarkExecuting {

        @Test
        @DisplayName("PENDING → EXECUTING 전이 성공")
        void givenPending_whenMarkExecuting_thenExecuting() {
            // Given
            ScheduledMessage msg = pendingMessage();

            // When
            msg.markExecuting();

            // Then
            assertThat(msg.getStatus()).isEqualTo(ScheduleStatus.EXECUTING);
        }
    }

    // ── markExecuted() ────────────────────────────────────────────────

    @Nested
    @DisplayName("발송 완료 (markExecuted)")
    class MarkExecuted {

        @Test
        @DisplayName("EXECUTING → EXECUTED 전이 성공, executedAt 기록")
        void givenExecuting_whenMarkExecuted_thenExecuted() {
            // Given
            ScheduledMessage msg = pendingMessage();
            msg.markExecuting();

            // When
            msg.markExecuted();

            // Then
            assertThat(msg.getStatus()).isEqualTo(ScheduleStatus.EXECUTED);
            assertThat(msg.getExecutedAt()).isNotNull();
        }
    }

    // ── markFailed() ──────────────────────────────────────────────────

    @Nested
    @DisplayName("발송 실패 처리 (markFailed)")
    class MarkFailed {

        @Test
        @DisplayName("retryCount=0 에서 실패 - PENDING 재전이, retryCount=1")
        void givenRetry0_whenFailed_thenPendingAndRetryIncremented() {
            // Given
            ScheduledMessage msg = messageWithRetry(0);
            msg.markExecuting();

            // When
            msg.markFailed();

            // Then
            assertThat(msg.getStatus()).isEqualTo(ScheduleStatus.PENDING);
            assertThat(msg.getRetryCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("retryCount=1 에서 실패 - PENDING 재전이, retryCount=2")
        void givenRetry1_whenFailed_thenPendingAndRetryIncremented() {
            // Given
            ScheduledMessage msg = messageWithRetry(1);
            msg.markExecuting();

            // When
            msg.markFailed();

            // Then
            assertThat(msg.getStatus()).isEqualTo(ScheduleStatus.PENDING);
            assertThat(msg.getRetryCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("retryCount=2 에서 실패 - FAILED 전이, retryCount=3 (MAX_RETRY 도달)")
        void givenRetry2_whenFailed_thenFailed() {
            // Given
            ScheduledMessage msg = messageWithRetry(2);
            msg.markExecuting();

            // When
            msg.markFailed();

            // Then
            assertThat(msg.getStatus()).isEqualTo(ScheduleStatus.FAILED);
            assertThat(msg.getRetryCount()).isEqualTo(ScheduledMessage.MAX_RETRY);
        }
    }

    // ── isRetryable() ─────────────────────────────────────────────────

    @Nested
    @DisplayName("재시도 가능 여부 (isRetryable)")
    class IsRetryable {

        @Test
        @DisplayName("retryCount=0 - 재시도 가능")
        void givenRetry0_thenRetryable() {
            assertThat(messageWithRetry(0).isRetryable()).isTrue();
        }

        @Test
        @DisplayName("retryCount=2 - 재시도 가능 (MAX_RETRY-1)")
        void givenRetry2_thenRetryable() {
            assertThat(messageWithRetry(2).isRetryable()).isTrue();
        }

        @Test
        @DisplayName("retryCount=3 (MAX_RETRY) - 재시도 불가")
        void givenRetry3_thenNotRetryable() {
            assertThat(messageWithRetry(ScheduledMessage.MAX_RETRY).isRetryable()).isFalse();
        }
    }
}