package com.example.chat.scheduled.application.service;

import com.example.chat.message.application.service.MessageSendService;
import com.example.chat.message.domain.MessageContent;
import com.example.chat.scheduled.domain.model.ScheduleStatus;
import com.example.chat.scheduled.domain.model.ScheduleType;
import com.example.chat.scheduled.domain.model.ScheduledMessage;
import com.example.chat.scheduled.domain.repository.ScheduledMessageRepository;
import com.example.chat.scheduled.infrastructure.quartz.QuartzJobScheduler;
import com.example.chat.scheduled.rest.dto.request.CreateScheduledMessageRequest;
import com.example.chat.scheduled.rest.dto.response.ScheduledMessageResponse;
import com.example.chat.storage.domain.repository.JpaChannelMemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * [단위 테스트] ScheduledMessageCommandServiceImpl
 *
 * TDD Red Phase — ScheduledMessageCommandServiceImpl 구현 전 작성
 * 이 테스트를 Green으로 만드는 것이 구현 목표.
 *
 * 검증 범위:
 * - createScheduledMessage: 정상 생성, 시간 범위 오류, 한도 초과, 채널 미가입
 * - cancelScheduledMessage:  정상 취소, EXECUTED 취소 시도, 본인 아닌 취소 시도, 예약 없음
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ScheduledMessageCommandServiceImpl 단위 테스트")
class ScheduledMessageCommandServiceImplTest {

    @Mock ScheduledMessageRepository scheduleRepository;
    @Mock JpaChannelMemberRepository channelMemberRepository;
    @Mock QuartzJobScheduler quartzJobScheduler;
    @Mock MessageSendService messageSendService;

    @InjectMocks ScheduledMessageCommandServiceImpl service;

    // ── Fixtures ──────────────────────────────────────────────────────

    private static final String SENDER_ID  = "user-001";
    private static final String CHANNEL_ID = "ch-001";
    private static final String SCHEDULE_ID = "sched-001";
    private static final ZonedDateTime NOW = ZonedDateTime.now();

    private CreateScheduledMessageRequest textRequest(ZonedDateTime scheduledAt) {
        return new CreateScheduledMessageRequest(
                CHANNEL_ID, "TEXT", "예약 메시지", null, null, null, null, scheduledAt);
    }

    private ScheduledMessage pendingSchedule() {
        return new ScheduledMessage(
                SCHEDULE_ID, CHANNEL_ID, SENDER_ID,
                MessageContent.text("예약 메시지"),
                ScheduleType.ONCE, ScheduleStatus.PENDING,
                NOW.plusHours(1), NOW, null, null, 0
        );
    }

    private ScheduledMessage executedSchedule() {
        var m = pendingSchedule();
        m.markExecuting();
        m.markExecuted();
        return m;
    }

    // ── createScheduledMessage ────────────────────────────────────────

    @Nested
    @DisplayName("예약 생성 (createScheduledMessage)")
    class CreateScheduledMessage {

        @Test
        @DisplayName("정상 예약 생성 - 저장 및 Quartz 등록 후 201 응답")
        void givenValidRequest_whenCreate_thenSavedAndScheduled() throws Exception {
            // Given
            ZonedDateTime scheduledAt = NOW.plusHours(2);
            CreateScheduledMessageRequest request = textRequest(scheduledAt);

            when(channelMemberRepository.existsByChannelIdAndUserId(CHANNEL_ID, SENDER_ID))
                    .thenReturn(true);
            when(scheduleRepository.countByChannelIdAndSenderIdAndStatusAndScheduledAtBetween(
                    eq(CHANNEL_ID), eq(SENDER_ID), eq(ScheduleStatus.PENDING), any(), any()))
                    .thenReturn(0L);
            when(scheduleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // When
            ScheduledMessageResponse response = service.createScheduledMessage(SENDER_ID, request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.channelId()).isEqualTo(CHANNEL_ID);
            assertThat(response.status()).isEqualTo(ScheduleStatus.PENDING);

            ArgumentCaptor<ScheduledMessage> captor = ArgumentCaptor.forClass(ScheduledMessage.class);
            verify(scheduleRepository).save(captor.capture());
            assertThat(captor.getValue().getSenderId()).isEqualTo(SENDER_ID);
            assertThat(captor.getValue().getScheduledAt()).isEqualTo(scheduledAt);

            verify(quartzJobScheduler).schedule(any(ScheduledMessage.class));
        }

        @Test
        @DisplayName("scheduledAt이 현재 시각 + 5분 이내 - 예외 발생 (SCHEDULED_AT_PAST)")
        void givenTooSoon_whenCreate_thenException() {
            // Given
            CreateScheduledMessageRequest request = textRequest(NOW.plusMinutes(3));

            when(channelMemberRepository.existsByChannelIdAndUserId(CHANNEL_ID, SENDER_ID))
                    .thenReturn(true);

            // When / Then
            assertThatThrownBy(() -> service.createScheduledMessage(SENDER_ID, request))
                    .isInstanceOf(RuntimeException.class);

            verify(scheduleRepository, never()).save(any());
        }

        @Test
        @DisplayName("scheduledAt이 현재 시각 + 30일 초과 - 예외 발생 (SCHEDULED_AT_TOO_FAR)")
        void givenTooFar_whenCreate_thenException() {
            // Given
            CreateScheduledMessageRequest request = textRequest(NOW.plusDays(31));

            when(channelMemberRepository.existsByChannelIdAndUserId(CHANNEL_ID, SENDER_ID))
                    .thenReturn(true);

            // When / Then
            assertThatThrownBy(() -> service.createScheduledMessage(SENDER_ID, request))
                    .isInstanceOf(RuntimeException.class);

            verify(scheduleRepository, never()).save(any());
        }


        @Test
        @DisplayName("채널 일일 PENDING 한도 10개 초과 - 예외 발생 (SCHEDULE_LIMIT_EXCEEDED)")
        void givenLimitExceeded_whenCreate_thenException() {
            // Given
            CreateScheduledMessageRequest request = textRequest(NOW.plusHours(2));

            when(channelMemberRepository.existsByChannelIdAndUserId(CHANNEL_ID, SENDER_ID))
                    .thenReturn(true);
            when(scheduleRepository.countByChannelIdAndSenderIdAndStatusAndScheduledAtBetween(
                    eq(CHANNEL_ID), eq(SENDER_ID), eq(ScheduleStatus.PENDING), any(), any()))
                    .thenReturn(10L);

            // When / Then
            assertThatThrownBy(() -> service.createScheduledMessage(SENDER_ID, request))
                    .isInstanceOf(RuntimeException.class);

            verify(scheduleRepository, never()).save(any());
        }

        @Test
        @DisplayName("채널 미가입 사용자 예약 시도 - 예외 발생 (CHANNEL_NOT_MEMBER)")
        void givenNotMember_whenCreate_thenException() {
            // Given
            CreateScheduledMessageRequest request = textRequest(NOW.plusHours(2));

            when(channelMemberRepository.existsByChannelIdAndUserId(CHANNEL_ID, SENDER_ID))
                    .thenReturn(false);

            // When / Then
            assertThatThrownBy(() -> service.createScheduledMessage(SENDER_ID, request))
                    .isInstanceOf(RuntimeException.class);

            verify(scheduleRepository, never()).save(any());
        }

        @Test
        @DisplayName("경계값: scheduledAt = 정확히 now + 5분 1초 - 생성 성공")
        void givenJustOverMinBoundary_whenCreate_thenSuccess() throws Exception {
            // Given
            CreateScheduledMessageRequest request = textRequest(NOW.plusMinutes(5).plusSeconds(1));

            when(channelMemberRepository.existsByChannelIdAndUserId(CHANNEL_ID, SENDER_ID))
                    .thenReturn(true);
            when(scheduleRepository.countByChannelIdAndSenderIdAndStatusAndScheduledAtBetween(
                    any(), any(), any(), any(), any())).thenReturn(0L);
            when(scheduleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // When
            ScheduledMessageResponse response = service.createScheduledMessage(SENDER_ID, request);

            // Then
            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("경계값: 오늘 한도 9개 → 10번째 생성 성공")
        void givenCount9_whenCreate_thenSuccess() throws Exception {
            // Given
            CreateScheduledMessageRequest request = textRequest(NOW.plusHours(1));

            when(channelMemberRepository.existsByChannelIdAndUserId(CHANNEL_ID, SENDER_ID))
                    .thenReturn(true);
            when(scheduleRepository.countByChannelIdAndSenderIdAndStatusAndScheduledAtBetween(
                    any(), any(), any(), any(), any())).thenReturn(9L);
            when(scheduleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // When
            ScheduledMessageResponse response = service.createScheduledMessage(SENDER_ID, request);

            // Then
            assertThat(response).isNotNull();
        }
    }

    // ── cancelScheduledMessage ────────────────────────────────────────

    @Nested
    @DisplayName("예약 취소 (cancelScheduledMessage)")
    class CancelScheduledMessage {

        @Test
        @DisplayName("PENDING 상태 예약 취소 - CANCELLED 전이 및 저장")
        void givenPending_whenCancel_thenCancelledAndSaved() {
            // Given
            ScheduledMessage pending = pendingSchedule();
            when(scheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(pending));
            when(scheduleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // When
            service.cancelScheduledMessage(SCHEDULE_ID, SENDER_ID);

            // Then
            ArgumentCaptor<ScheduledMessage> captor = ArgumentCaptor.forClass(ScheduledMessage.class);
            verify(scheduleRepository).save(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(ScheduleStatus.CANCELLED);
            assertThat(captor.getValue().getCancelledAt()).isNotNull();
        }

        @Test
        @DisplayName("EXECUTED 상태 예약 취소 시도 - 예외 발생 (SCHEDULE_NOT_CANCELLABLE)")
        void givenExecuted_whenCancel_thenException() {
            // Given
            ScheduledMessage executed = executedSchedule();
            when(scheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(executed));

            // When / Then
            assertThatThrownBy(() -> service.cancelScheduledMessage(SCHEDULE_ID, SENDER_ID))
                    .isInstanceOf(RuntimeException.class);

            verify(scheduleRepository, never()).save(any());
        }

        @Test
        @DisplayName("본인이 아닌 사용자 취소 시도 - 예외 발생 (SCHEDULE_CANCEL_FORBIDDEN)")
        void givenOtherUser_whenCancel_thenException() {
            // Given
            ScheduledMessage pending = pendingSchedule();
            when(scheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(pending));

            // When / Then
            assertThatThrownBy(() -> service.cancelScheduledMessage(SCHEDULE_ID, "other-user"))
                    .isInstanceOf(RuntimeException.class);

            verify(scheduleRepository, never()).save(any());
        }

        @Test
        @DisplayName("존재하지 않는 예약 취소 - 예외 발생 (SCHEDULE_NOT_FOUND)")
        void givenNotFound_whenCancel_thenException() {
            // Given
            when(scheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> service.cancelScheduledMessage(SCHEDULE_ID, SENDER_ID))
                    .isInstanceOf(RuntimeException.class);
        }
    }
}
