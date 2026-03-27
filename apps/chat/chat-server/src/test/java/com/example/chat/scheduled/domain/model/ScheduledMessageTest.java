package com.example.chat.scheduled.domain.model;

import com.example.chat.scheduled.fixture.ScheduledMessageFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.example.chat.scheduled.fixture.ScheduledMessageFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * [단위 테스트] ScheduledMessage 도메인 모델
 *
 * 검증 범위: 상태 전이 불변식, 상태 가드, 재시도 횟수 관리
 */
@DisplayName("ScheduledMessage 도메인 단위 테스트")
class ScheduledMessageTest {

    // ── cancel() ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("예약 취소 (cancel)")
    class Cancel {

        @Test
        @DisplayName("PENDING 상태에서 취소 - CANCELLED 전이 성공")
        void givenPending_whenCancel_thenCancelled() {
            var msg = pending();
            msg.cancel();
            assertThat(msg.getStatus()).isEqualTo(ScheduleStatus.CANCELLED);
            assertThat(msg.getCancelledAt()).isNotNull();
        }

        @Test
        @DisplayName("EXECUTED 상태에서 취소 - IllegalStateException 발생")
        void givenExecuted_whenCancel_thenException() {
            var msg = executed();
            assertThatThrownBy(msg::cancel)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("PENDING");
        }

        @Test
        @DisplayName("EXECUTING 상태에서 취소 - IllegalStateException 발생")
        void givenExecuting_whenCancel_thenException() {
            var msg = executing();
            assertThatThrownBy(msg::cancel)
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("CANCELLED 상태에서 재취소 - IllegalStateException 발생")
        void givenCancelled_whenCancelAgain_thenException() {
            var msg = cancelled();
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
            var msg = pending();
            msg.markExecuting();
            assertThat(msg.getStatus()).isEqualTo(ScheduleStatus.EXECUTING);
        }

        @Test
        @DisplayName("CANCELLED 상태에서 실행 시작 - IllegalStateException 발생")
        void givenCancelled_whenMarkExecuting_thenException() {
            var msg = cancelled();
            assertThatThrownBy(msg::markExecuting)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("PENDING");
        }

        @Test
        @DisplayName("EXECUTED 상태에서 실행 시작 - IllegalStateException 발생")
        void givenExecuted_whenMarkExecuting_thenException() {
            var msg = executed();
            assertThatThrownBy(msg::markExecuting)
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    // ── markExecuted() ────────────────────────────────────────────────

    @Nested
    @DisplayName("발송 완료 (markExecuted)")
    class MarkExecuted {

        @Test
        @DisplayName("EXECUTING → EXECUTED 전이 성공, executedAt 기록")
        void givenExecuting_whenMarkExecuted_thenExecuted() {
            var msg = executing();
            msg.markExecuted();
            assertThat(msg.getStatus()).isEqualTo(ScheduleStatus.EXECUTED);
            assertThat(msg.getExecutedAt()).isNotNull();
        }

        @Test
        @DisplayName("PENDING 상태에서 완료 전이 시도 - IllegalStateException 발생")
        void givenPending_whenMarkExecuted_thenException() {
            var msg = pending();
            assertThatThrownBy(msg::markExecuted)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("EXECUTING");
        }
    }

    // ── markFailed() ──────────────────────────────────────────────────

    @Nested
    @DisplayName("발송 실패 처리 (markFailed)")
    class MarkFailed {

        @Test
        @DisplayName("retryCount=0 에서 실패 - PENDING 재전이, retryCount=1")
        void givenRetry0_whenFailed_thenPendingAndRetryIncremented() {
            var msg = pendingWithRetry(0);
            msg.markExecuting();
            msg.markFailed();
            assertThat(msg.getStatus()).isEqualTo(ScheduleStatus.PENDING);
            assertThat(msg.getRetryCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("retryCount=1 에서 실패 - PENDING 재전이, retryCount=2")
        void givenRetry1_whenFailed_thenPendingAndRetryIncremented() {
            var msg = pendingWithRetry(1);
            msg.markExecuting();
            msg.markFailed();
            assertThat(msg.getStatus()).isEqualTo(ScheduleStatus.PENDING);
            assertThat(msg.getRetryCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("retryCount=2 에서 실패 - FAILED 전이, retryCount=3 (MAX_RETRY 도달)")
        void givenRetry2_whenFailed_thenFailed() {
            var msg = pendingWithRetry(2);
            msg.markExecuting();
            msg.markFailed();
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
            assertThat(ScheduledMessageFixture.pendingWithRetry(0).isRetryable()).isTrue();
        }

        @Test
        @DisplayName("retryCount=2 - 재시도 가능 (MAX_RETRY-1)")
        void givenRetry2_thenRetryable() {
            assertThat(ScheduledMessageFixture.pendingWithRetry(2).isRetryable()).isTrue();
        }

        @Test
        @DisplayName("retryCount=3 (MAX_RETRY) - 재시도 불가")
        void givenRetry3_thenNotRetryable() {
            assertThat(ScheduledMessageFixture.pendingWithRetry(ScheduledMessage.MAX_RETRY).isRetryable()).isFalse();
        }
    }
}
