package com.example.chat.domain.service;

import com.example.chat.domain.channel.ChannelId;
import com.example.chat.domain.message.Message;
import com.example.chat.domain.message.MessageContent;
import com.example.chat.domain.message.MessageType;
import com.example.chat.domain.schedule.CronExpression;
import com.example.chat.domain.schedule.ScheduleRule;
import com.example.chat.domain.schedule.ScheduleType;
import com.example.chat.domain.user.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * ScheduleDomainService 단위 테스트
 */
@DisplayName("ScheduleDomainService 단위 테스트")
class ScheduleDomainServiceTest {

	private ScheduleDomainService scheduleDomainService;

	@BeforeEach
	void setUp() {
		scheduleDomainService = new ScheduleDomainService();
	}

	// ============================================================
	// 단발성 스케줄 생성 테스트
	// ============================================================

	private Message createTextMessage() {
		ChannelId channelId = ChannelId.generate();
		UserId senderId = UserId.of("test-user-id");
		MessageContent content = MessageContent.text("Test message");
		return Message.create(channelId, senderId, content, MessageType.TEXT);
	}

	// ============================================================
	// 주기적 스케줄 생성 테스트
	// ============================================================

	private Message createImageMessage() {
		ChannelId channelId = ChannelId.generate();
		UserId senderId = UserId.of("test-user-id");
		MessageContent content = MessageContent.image("https://example.com/image.jpg", "image.jpg", 1024L);
		return Message.create(channelId, senderId, content, MessageType.IMAGE);
	}

	@Nested
	@DisplayName("단발성 스케줄 생성")
	class CreateOneTimeSchedule {

		@Test
		@DisplayName("정상: 단발성 스케줄 생성")
		void success_createOneTimeSchedule() {
			// Given
			Message message = createTextMessage();
			Instant scheduledAt = Instant.now().plus(1, ChronoUnit.HOURS);

			// When
			ScheduleRule schedule = scheduleDomainService.createOneTimeSchedule(message, scheduledAt);

			// Then
			assertThat(schedule).isNotNull();
			assertThat(schedule.getType()).isEqualTo(ScheduleType.ONE_TIME);
			assertThat(schedule.getScheduledAt()).isEqualTo(scheduledAt);
			assertThat(schedule.getMessage()).isEqualTo(message);
			assertThat(schedule.getCronExpression()).isNull();
		}

		@Test
		@DisplayName("정상: 1분 후 예약")
		void success_oneMinuteLater() {
			// Given
			Message message = createTextMessage();
			Instant scheduledAt = Instant.now().plus(1, ChronoUnit.MINUTES);

			// When
			ScheduleRule schedule = scheduleDomainService.createOneTimeSchedule(message, scheduledAt);

			// Then
			assertThat(schedule).isNotNull();
			assertThat(schedule.getScheduledAt()).isAfter(Instant.now());
		}

		@Test
		@DisplayName("경계값: 1년 후 예약 (최대값)")
		void boundary_oneYearLater() {
			// Given
			Message message = createTextMessage();
			Instant scheduledAt = Instant.now().plus(365, ChronoUnit.DAYS);

			// When
			ScheduleRule schedule = scheduleDomainService.createOneTimeSchedule(message, scheduledAt);

			// Then
			assertThat(schedule).isNotNull();
			assertThat(schedule.getScheduledAt()).isEqualTo(scheduledAt);
		}

		@Test
		@DisplayName("실패: 스케줄 시간이 null")
		void fail_nullScheduledTime() {
			// Given
			Message message = createTextMessage();

			// When & Then
			assertThatThrownBy(() -> scheduleDomainService.createOneTimeSchedule(message, null))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("Scheduled time cannot be null");
		}

		@Test
		@DisplayName("실패: 과거 시간으로 예약")
		void fail_pastTime() {
			// Given
			Message message = createTextMessage();
			Instant pastTime = Instant.now().minus(1, ChronoUnit.HOURS);

			// When & Then
			assertThatThrownBy(() -> scheduleDomainService.createOneTimeSchedule(message, pastTime))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("Scheduled time must be in the future");
		}

		@Test
		@DisplayName("실패: 1년 초과 미래 시간")
		void fail_tooFarFuture() {
			// Given
			Message message = createTextMessage();
			Instant tooFarFuture = Instant.now().plus(366, ChronoUnit.DAYS);

			// When & Then
			assertThatThrownBy(() -> scheduleDomainService.createOneTimeSchedule(message, tooFarFuture))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("Scheduled time cannot be more than 1 year in the future");
		}

		@Test
		@DisplayName("실패: 현재 시간 (정확히 현재)")
		void fail_exactlyNow() {
			// Given
			Message message = createTextMessage();
			Instant now = Instant.now();

			// When & Then
			// 실행 시간 차이로 인해 실패할 수 있으므로, 약간의 여유를 둠
			assertThatThrownBy(() -> scheduleDomainService.createOneTimeSchedule(message, now))
					.isInstanceOf(IllegalArgumentException.class);
		}
	}

	@Nested
	@DisplayName("주기적 스케줄 생성")
	class CreateRecurringSchedule {

		@Test
		@DisplayName("정상: 매일 오전 9시 실행")
		void success_dailyAt9AM() {
			// Given
			Message message = createTextMessage();
			CronExpression cronExpression = CronExpression.of("0 0 9 * * ?"); // 매일 오전 9시

			// When
			ScheduleRule schedule = scheduleDomainService.createRecurringSchedule(message, cronExpression);

			// Then
			assertThat(schedule).isNotNull();
			assertThat(schedule.getType()).isEqualTo(ScheduleType.RECURRING);
			assertThat(schedule.getCronExpression()).isEqualTo(cronExpression);
			assertThat(schedule.getMessage()).isEqualTo(message);
			assertThat(schedule.getScheduledAt()).isNull();
		}

		@Test
		@DisplayName("정상: 매주 월요일 오전 10시")
		void success_mondayAt10AM() {
			// Given
			Message message = createTextMessage();
			CronExpression cronExpression = CronExpression.of("0 0 10 ? * MON"); // 매주 월요일 오전 10시

			// When
			ScheduleRule schedule = scheduleDomainService.createRecurringSchedule(message, cronExpression);

			// Then
			assertThat(schedule).isNotNull();
			assertThat(schedule.getCronExpression().getValue()).isEqualTo("0 0 10 ? * MON");
		}

		@Test
		@DisplayName("정상: 매달 1일 오전 12시")
		void success_firstDayOfMonth() {
			// Given
			Message message = createTextMessage();
			CronExpression cronExpression = CronExpression.of("0 0 12 1 * ?"); // 매달 1일 오전 12시

			// When
			ScheduleRule schedule = scheduleDomainService.createRecurringSchedule(message, cronExpression);

			// Then
			assertThat(schedule).isNotNull();
			assertThat(schedule.getType()).isEqualTo(ScheduleType.RECURRING);
		}

		@Test
		@DisplayName("정상: 매 1분마다 실행")
		void success_everyMinute() {
			// Given
			Message message = createTextMessage();
			CronExpression cronExpression = CronExpression.of("0 * * * * ?"); // 매 1분마다

			// When
			ScheduleRule schedule = scheduleDomainService.createRecurringSchedule(message, cronExpression);

			// Then
			assertThat(schedule).isNotNull();
			assertThat(schedule.getCronExpression().getValue()).isEqualTo("0 * * * * ?");
		}

		@Test
		@DisplayName("실패: CronExpression이 null")
		void fail_nullCronExpression() {
			// Given
			Message message = createTextMessage();

			// When & Then
			assertThatThrownBy(() -> scheduleDomainService.createRecurringSchedule(message, null))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("Cron expression cannot be null");
		}
	}

	@Nested
	@DisplayName("복합 시나리오 테스트")
	class ComplexScenarios {

		@Test
		@DisplayName("동일한 메시지로 단발성 + 주기적 스케줄 각각 생성")
		void success_sameMessageDifferentSchedules() {
			// Given
			Message message = createTextMessage();

			// When
			ScheduleRule oneTimeSchedule = scheduleDomainService.createOneTimeSchedule(
					message,
					Instant.now().plus(1, ChronoUnit.HOURS)
			);

			ScheduleRule recurringSchedule = scheduleDomainService.createRecurringSchedule(
					message,
					CronExpression.of("0 0 9 * * ?")
			);

			// Then
			assertThat(oneTimeSchedule.getType()).isEqualTo(ScheduleType.ONE_TIME);
			assertThat(recurringSchedule.getType()).isEqualTo(ScheduleType.RECURRING);
			assertThat(oneTimeSchedule.getMessage()).isEqualTo(recurringSchedule.getMessage());
		}

		@Test
		@DisplayName("다양한 메시지 타입으로 스케줄 생성")
		void success_differentMessageTypes() {
			// Given
			Message textMessage = createTextMessage();
			Message imageMessage = createImageMessage();

			// When
			ScheduleRule textSchedule = scheduleDomainService.createOneTimeSchedule(
					textMessage,
					Instant.now().plus(1, ChronoUnit.HOURS)
			);

			ScheduleRule imageSchedule = scheduleDomainService.createRecurringSchedule(
					imageMessage,
					CronExpression.of("0 0 9 * * ?")
			);

			// Then
			assertThat(textSchedule.getMessage().getType()).isEqualTo(MessageType.TEXT);
			assertThat(imageSchedule.getMessage().getType()).isEqualTo(MessageType.IMAGE);
		}

		@Test
		@DisplayName("여러 시간대 단발성 스케줄 생성")
		void success_multipleFutureTimes() {
			// Given
			Message message = createTextMessage();

			// When
			ScheduleRule schedule1Hour = scheduleDomainService.createOneTimeSchedule(
					message,
					Instant.now().plus(1, ChronoUnit.HOURS)
			);

			ScheduleRule schedule1Day = scheduleDomainService.createOneTimeSchedule(
					message,
					Instant.now().plus(1, ChronoUnit.DAYS)
			);

			ScheduleRule schedule1Week = scheduleDomainService.createOneTimeSchedule(
					message,
					Instant.now().plus(7, ChronoUnit.DAYS)
			);

			// Then
			assertThat(schedule1Hour.getScheduledAt()).isBefore(schedule1Day.getScheduledAt());
			assertThat(schedule1Day.getScheduledAt()).isBefore(schedule1Week.getScheduledAt());
		}
	}
}
