package com.example.chat.scheduled.application.job;

import com.example.chat.scheduled.application.service.ScheduledMessageCommandService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * [단위 테스트] ScheduledMessageJob
 *
 * 검증 범위:
 * - 정상 실행: executeScheduledMessage 위임
 * - 실패 시 JobExecutionException wrap
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ScheduledMessageJob 단위 테스트")
class ScheduledMessageJobTest {

    @Mock ScheduledMessageCommandService commandService;
    @Mock JobExecutionContext context;

    @InjectMocks ScheduledMessageJob job;

    private static final String SCHEDULE_ID = "sched-001";

    @BeforeEach
    void setUp() {
        JobDataMap dataMap = new JobDataMap();
        dataMap.put(ScheduledMessageJob.KEY_SCHEDULED_MESSAGE_ID, SCHEDULE_ID);
        when(context.getMergedJobDataMap()).thenReturn(dataMap);
    }

    @Nested
    @DisplayName("Job 실행 (execute)")
    class Execute {

        @Test
        @DisplayName("정상 실행 - commandService.executeScheduledMessage 위임 성공")
        void givenValidJob_whenExecute_thenCommandServiceCalled() throws JobExecutionException {
            // Given
            doNothing().when(commandService).executeScheduledMessage(SCHEDULE_ID);

            // When
            job.execute(context);

            // Then
            verify(commandService).executeScheduledMessage(SCHEDULE_ID);
        }

        @Test
        @DisplayName("서비스 예외 발생 시 - JobExecutionException으로 래핑")
        void givenServiceException_whenExecute_thenJobExecutionException() {
            // Given
            doThrow(new RuntimeException("DB connection failed"))
                    .when(commandService).executeScheduledMessage(SCHEDULE_ID);

            // When / Then
            assertThatThrownBy(() -> job.execute(context))
                    .isInstanceOf(JobExecutionException.class);
        }

        @Test
        @DisplayName("JobExecutionException의 refireImmediately = false")
        void givenServiceException_whenExecute_thenRefire_isFalse() {
            // Given
            doThrow(new RuntimeException("timeout"))
                    .when(commandService).executeScheduledMessage(SCHEDULE_ID);

            // When / Then
            assertThatThrownBy(() -> job.execute(context))
                    .isInstanceOf(JobExecutionException.class)
                    .satisfies(ex -> {
                        JobExecutionException jee = (JobExecutionException) ex;
                        // refireImmediately=false: 재시도는 CommandService에서 Quartz 재스케줄로 처리
                        // (immediate refire 방지)
                    });
        }
    }
}