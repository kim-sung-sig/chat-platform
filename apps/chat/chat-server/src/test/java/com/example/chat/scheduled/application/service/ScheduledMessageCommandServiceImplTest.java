package com.example.chat.scheduled.application.service;

import com.example.chat.common.core.exception.ChatErrorCode;
import com.example.chat.message.application.service.MessageSendService;
import com.example.chat.scheduled.domain.model.ScheduleStatus;
import com.example.chat.scheduled.domain.model.ScheduledMessage;
import com.example.chat.scheduled.domain.repository.ChannelMemberRepository;
import com.example.chat.scheduled.domain.repository.ScheduledMessageRepository;
import com.example.chat.scheduled.infrastructure.quartz.QuartzJobScheduler;
import com.example.chat.scheduled.rest.dto.response.ScheduledMessageResponse;
import com.example.chat.shared.exception.ChatException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static com.example.chat.scheduled.fixture.ScheduledMessageFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * [단위 테스트] ScheduledMessageCommandServiceImpl
 *
 * 검증 범위:
 * - createScheduledMessage: 정상 생성, 시간 범위 오류, 한도 초과, 채널 미가입
 * - cancelScheduledMessage: 정상 취소, EXECUTED 취소 시도, 본인 아닌 취소 시도, 예약 없음
 * - executeScheduledMessage: 정상 실행, 발송 실패 → FAILED 상태 기록, 재시도 예약
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ScheduledMessageCommandServiceImpl 단위 테스트")
class ScheduledMessageCommandServiceImplTest {

    @Mock ScheduledMessageRepository scheduleRepository;
    @Mock ChannelMemberRepository channelMemberRepository;
    @Mock QuartzJobScheduler quartzJobScheduler;
    @Mock MessageSendService messageSendService;
    @Mock ApplicationEventPublisher eventPublisher;

    @InjectMocks ScheduledMessageCommandServiceImpl service;

    // ── createScheduledMessage ────────────────────────────────────────

    @Nested
    @DisplayName("예약 생성 (createScheduledMessage)")
    class CreateScheduledMessage {

        @Test
        @DisplayName("정상 예약 생성 - 저장 및 Quartz 등록 후 응답 반환")
        void givenValidRequest_whenCreate_thenSavedAndScheduled() throws Exception {
            var request = textRequest(BASE_NOW.plusHours(2));

            when(channelMemberRepository.existsByChannelIdAndUserId(CHANNEL_ID, SENDER_ID)).thenReturn(true);
            when(scheduleRepository.countByChannelIdAndSenderIdAndStatusAndScheduledAtBetween(
                    eq(CHANNEL_ID), eq(SENDER_ID), eq(ScheduleStatus.PENDING), any(), any())).thenReturn(0L);
            when(scheduleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            ScheduledMessageResponse response = service.createScheduledMessage(SENDER_ID, request);

            assertThat(response.channelId()).isEqualTo(CHANNEL_ID);
            assertThat(response.status()).isEqualTo(ScheduleStatus.PENDING);
            ArgumentCaptor<ScheduledMessage> captor = ArgumentCaptor.forClass(ScheduledMessage.class);
            verify(scheduleRepository).save(captor.capture());
            assertThat(captor.getValue().getSenderId()).isEqualTo(SENDER_ID);
            verify(quartzJobScheduler).schedule(any(ScheduledMessage.class));
        }

        @Test
        @DisplayName("scheduledAt이 현재 시각 + 5분 이내 - SCHEDULE_INVALID_TIME 예외")
        void givenTooSoon_whenCreate_thenScheduleInvalidTimeException() {
            var request = textRequest(BASE_NOW.plusMinutes(3));
            when(channelMemberRepository.existsByChannelIdAndUserId(CHANNEL_ID, SENDER_ID)).thenReturn(true);

            assertThatThrownBy(() -> service.createScheduledMessage(SENDER_ID, request))
                    .isInstanceOf(ChatException.class)
                    .satisfies(e -> assertThat(((ChatException) e).getErrorCode())
                            .isEqualTo(ChatErrorCode.SCHEDULE_INVALID_TIME));
            verify(scheduleRepository, never()).save(any());
        }

        @Test
        @DisplayName("scheduledAt이 현재 시각 + 30일 초과 - SCHEDULE_INVALID_TIME 예외")
        void givenTooFar_whenCreate_thenScheduleInvalidTimeException() {
            var request = textRequest(BASE_NOW.plusDays(31));
            when(channelMemberRepository.existsByChannelIdAndUserId(CHANNEL_ID, SENDER_ID)).thenReturn(true);

            assertThatThrownBy(() -> service.createScheduledMessage(SENDER_ID, request))
                    .isInstanceOf(ChatException.class)
                    .satisfies(e -> assertThat(((ChatException) e).getErrorCode())
                            .isEqualTo(ChatErrorCode.SCHEDULE_INVALID_TIME));
            verify(scheduleRepository, never()).save(any());
        }

        @Test
        @DisplayName("채널 일일 PENDING 한도 10개 초과 - SCHEDULE_LIMIT_EXCEEDED 예외")
        void givenLimitExceeded_whenCreate_thenLimitExceededException() {
            var request = textRequest(BASE_NOW.plusHours(2));
            when(channelMemberRepository.existsByChannelIdAndUserId(CHANNEL_ID, SENDER_ID)).thenReturn(true);
            when(scheduleRepository.countByChannelIdAndSenderIdAndStatusAndScheduledAtBetween(
                    eq(CHANNEL_ID), eq(SENDER_ID), eq(ScheduleStatus.PENDING), any(), any())).thenReturn(10L);

            assertThatThrownBy(() -> service.createScheduledMessage(SENDER_ID, request))
                    .isInstanceOf(ChatException.class)
                    .satisfies(e -> assertThat(((ChatException) e).getErrorCode())
                            .isEqualTo(ChatErrorCode.SCHEDULE_LIMIT_EXCEEDED));
            verify(scheduleRepository, never()).save(any());
        }

        @Test
        @DisplayName("채널 미가입 사용자 예약 시도 - CHANNEL_NOT_MEMBER 예외")
        void givenNotMember_whenCreate_thenChannelNotMemberException() {
            var request = textRequest(BASE_NOW.plusHours(2));
            when(channelMemberRepository.existsByChannelIdAndUserId(CHANNEL_ID, SENDER_ID)).thenReturn(false);

            assertThatThrownBy(() -> service.createScheduledMessage(SENDER_ID, request))
                    .isInstanceOf(ChatException.class)
                    .satisfies(e -> assertThat(((ChatException) e).getErrorCode())
                            .isEqualTo(ChatErrorCode.CHANNEL_NOT_MEMBER));
            verify(scheduleRepository, never()).save(any());
        }

        @Test
        @DisplayName("경계값: scheduledAt = now + 5분 1초 - 생성 성공")
        void givenJustOverMinBoundary_whenCreate_thenSuccess() throws Exception {
            var request = textRequest(BASE_NOW.plusMinutes(5).plusSeconds(1));
            when(channelMemberRepository.existsByChannelIdAndUserId(CHANNEL_ID, SENDER_ID)).thenReturn(true);
            when(scheduleRepository.countByChannelIdAndSenderIdAndStatusAndScheduledAtBetween(
                    any(), any(), any(), any(), any())).thenReturn(0L);
            when(scheduleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            assertThat(service.createScheduledMessage(SENDER_ID, request)).isNotNull();
        }

        @Test
        @DisplayName("경계값: 오늘 한도 9개 → 10번째 생성 성공")
        void givenCount9_whenCreate_thenSuccess() throws Exception {
            var request = textRequest(BASE_NOW.plusHours(1));
            when(channelMemberRepository.existsByChannelIdAndUserId(CHANNEL_ID, SENDER_ID)).thenReturn(true);
            when(scheduleRepository.countByChannelIdAndSenderIdAndStatusAndScheduledAtBetween(
                    any(), any(), any(), any(), any())).thenReturn(9L);
            when(scheduleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            assertThat(service.createScheduledMessage(SENDER_ID, request)).isNotNull();
        }
    }

    // ── cancelScheduledMessage ────────────────────────────────────────

    @Nested
    @DisplayName("예약 취소 (cancelScheduledMessage)")
    class CancelScheduledMessage {

        @Test
        @DisplayName("PENDING 상태 예약 취소 - CANCELLED 전이 및 저장")
        void givenPending_whenCancel_thenCancelledAndSaved() {
            when(scheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(pending()));
            when(scheduleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            service.cancelScheduledMessage(SCHEDULE_ID, SENDER_ID);

            ArgumentCaptor<ScheduledMessage> captor = ArgumentCaptor.forClass(ScheduledMessage.class);
            verify(scheduleRepository).save(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(ScheduleStatus.CANCELLED);
            assertThat(captor.getValue().getCancelledAt()).isNotNull();
        }

        @Test
        @DisplayName("EXECUTED 상태 예약 취소 시도 - SCHEDULE_NOT_CANCELLABLE 예외")
        void givenExecuted_whenCancel_thenNotCancellableException() {
            when(scheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(executed()));

            assertThatThrownBy(() -> service.cancelScheduledMessage(SCHEDULE_ID, SENDER_ID))
                    .isInstanceOf(ChatException.class)
                    .satisfies(e -> assertThat(((ChatException) e).getErrorCode())
                            .isEqualTo(ChatErrorCode.SCHEDULE_NOT_CANCELLABLE));
            verify(scheduleRepository, never()).save(any());
        }

        @Test
        @DisplayName("본인이 아닌 사용자 취소 시도 - SCHEDULE_CANCEL_FORBIDDEN 예외")
        void givenOtherUser_whenCancel_thenForbiddenException() {
            when(scheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(pendingByOtherUser()));

            assertThatThrownBy(() -> service.cancelScheduledMessage(SCHEDULE_ID, SENDER_ID))
                    .isInstanceOf(ChatException.class)
                    .satisfies(e -> assertThat(((ChatException) e).getErrorCode())
                            .isEqualTo(ChatErrorCode.SCHEDULE_CANCEL_FORBIDDEN));
            verify(scheduleRepository, never()).save(any());
        }

        @Test
        @DisplayName("존재하지 않는 예약 취소 - SCHEDULE_NOT_FOUND 예외")
        void givenNotFound_whenCancel_thenNotFoundException() {
            when(scheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.cancelScheduledMessage(SCHEDULE_ID, SENDER_ID))
                    .isInstanceOf(ChatException.class)
                    .satisfies(e -> assertThat(((ChatException) e).getErrorCode())
                            .isEqualTo(ChatErrorCode.SCHEDULE_NOT_FOUND));
        }
    }

    // ── executeScheduledMessage ───────────────────────────────────────

    @Nested
    @DisplayName("예약 실행 (executeScheduledMessage)")
    class ExecuteScheduledMessage {

        @Test
        @DisplayName("정상 실행 - EXECUTED 전이 및 저장")
        void givenPending_whenExecute_thenExecutedAndSaved() {
            var msg = pending();
            when(scheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(msg));
            when(scheduleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(channelMemberRepository.existsByChannelIdAndUserId(CHANNEL_ID, SENDER_ID)).thenReturn(true);

            service.executeScheduledMessage(SCHEDULE_ID);

            assertThat(msg.getStatus()).isEqualTo(ScheduleStatus.EXECUTED);
            assertThat(msg.getExecutedAt()).isNotNull();
            verify(messageSendService).sendScheduledMessage(
                    eq(SENDER_ID), eq(CHANNEL_ID), any());
        }

        @Test
        @DisplayName("발송 실패 - PENDING 재전이(retryCount=1), retryAt 예약")
        void givenSendFails_whenExecute_thenPendingAndRetryScheduled() throws Exception {
            var msg = pending();
            when(scheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(msg));
            when(scheduleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(channelMemberRepository.existsByChannelIdAndUserId(CHANNEL_ID, SENDER_ID)).thenReturn(true);
            doThrow(new RuntimeException("send failed"))
                    .when(messageSendService).sendScheduledMessage(any(), any(), any());

            assertThatThrownBy(() -> service.executeScheduledMessage(SCHEDULE_ID))
                    .isInstanceOf(RuntimeException.class);

            assertThat(msg.getStatus()).isEqualTo(ScheduleStatus.PENDING);
            assertThat(msg.getRetryCount()).isEqualTo(1);
            verify(quartzJobScheduler).scheduleRetry(eq(SCHEDULE_ID), any());
        }

        @Test
        @DisplayName("발송 실패 + MAX_RETRY 도달 - FAILED 전이, 재시도 예약 없음")
        void givenSendFailsAtMaxRetry_whenExecute_thenFailed() throws Exception {
            var msg = pendingWithRetry(2); // retryCount=2, 한 번 더 실패하면 MAX_RETRY(3) 도달
            when(scheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(msg));
            when(scheduleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(channelMemberRepository.existsByChannelIdAndUserId(CHANNEL_ID, SENDER_ID)).thenReturn(true);
            doThrow(new RuntimeException("send failed"))
                    .when(messageSendService).sendScheduledMessage(any(), any(), any());

            assertThatThrownBy(() -> service.executeScheduledMessage(SCHEDULE_ID))
                    .isInstanceOf(RuntimeException.class);

            assertThat(msg.getStatus()).isEqualTo(ScheduleStatus.FAILED);
            assertThat(msg.getRetryCount()).isEqualTo(ScheduledMessage.MAX_RETRY);
            verify(quartzJobScheduler, never()).scheduleRetry(any(), any());
        }

        @Test
        @DisplayName("발신자가 채널을 떠난 경우 - CANCELLED 전이, 재시도 없음")
        void givenSenderLeftChannel_whenExecute_thenCancelledWithoutRetry() throws Exception {
            var msg = pending();
            when(scheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(msg));
            when(scheduleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(channelMemberRepository.existsByChannelIdAndUserId(CHANNEL_ID, SENDER_ID)).thenReturn(false);

            service.executeScheduledMessage(SCHEDULE_ID);

            assertThat(msg.getStatus()).isEqualTo(ScheduleStatus.CANCELLED);
            verify(messageSendService, never()).sendScheduledMessage(any(), any(), any());
            verify(quartzJobScheduler, never()).scheduleRetry(any(), any());
        }

        @Test
        @DisplayName("존재하지 않는 예약 실행 - SCHEDULE_NOT_FOUND 예외")
        void givenNotFound_whenExecute_thenNotFoundException() {
            when(scheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.executeScheduledMessage(SCHEDULE_ID))
                    .isInstanceOf(ChatException.class)
                    .satisfies(e -> assertThat(((ChatException) e).getErrorCode())
                            .isEqualTo(ChatErrorCode.SCHEDULE_NOT_FOUND));
        }
    }
}
