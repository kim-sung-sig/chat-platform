package com.example.chat.scheduled.application.service;

import com.example.chat.common.core.exception.ChatErrorCode;
import com.example.chat.shared.exception.ChatException;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * 예약 발송 Command 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ScheduledMessageCommandServiceImpl implements ScheduledMessageCommandService {

    private static final int DAILY_LIMIT = 10;
    private static final int MIN_MINUTES_AHEAD = 5;
    private static final int MAX_DAYS_AHEAD = 30;

    private static final int RETRY_DELAY_SECONDS = 30;

    private final ScheduledMessageRepository scheduleRepository;
    private final JpaChannelMemberRepository channelMemberRepository;
    private final QuartzJobScheduler quartzJobScheduler;
    private final MessageSendService messageSendService;

    @Override
    public ScheduledMessageResponse createScheduledMessage(String senderId, CreateScheduledMessageRequest request) {
        // 채널 멤버 검증
        if (!channelMemberRepository.existsByChannelIdAndUserId(request.channelId(), senderId)) {
            throw new ChatException(ChatErrorCode.CHANNEL_NOT_MEMBER);
        }

        // 예약 시각 범위 검증
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime minTime = now.plusMinutes(MIN_MINUTES_AHEAD);
        ZonedDateTime maxTime = now.plusDays(MAX_DAYS_AHEAD);

        if (!request.scheduledAt().isAfter(minTime)) {
            throw new ChatException(ChatErrorCode.SCHEDULE_INVALID_TIME);
        }
        if (request.scheduledAt().isAfter(maxTime)) {
            throw new ChatException(ChatErrorCode.SCHEDULE_INVALID_TIME);
        }

        // 일일 한도 검증
        ZonedDateTime dayStart = now.toLocalDate().atStartOfDay(now.getZone());
        ZonedDateTime dayEnd = dayStart.plusDays(1);
        long count = scheduleRepository.countByChannelIdAndSenderIdAndStatusAndScheduledAtBetween(
                request.channelId(), senderId, ScheduleStatus.PENDING, dayStart, dayEnd);
        if (count >= DAILY_LIMIT) {
            throw new ChatException(ChatErrorCode.SCHEDULE_LIMIT_EXCEEDED);
        }

        // 도메인 객체 생성
        MessageContent content = buildContent(request);
        ScheduledMessage domain = new ScheduledMessage(
                UUID.randomUUID().toString(),
                request.channelId(),
                senderId,
                content,
                ScheduleType.ONCE,
                ScheduleStatus.PENDING,
                request.scheduledAt(),
                now,
                null,
                null,
                0
        );

        // 저장 및 Quartz 등록
        ScheduledMessage saved = scheduleRepository.save(domain);
        try {
            quartzJobScheduler.schedule(saved);
        } catch (SchedulerException e) {
            log.error("Quartz schedule failed for id={}", saved.getId(), e);
            throw new RuntimeException("예약 등록 중 오류가 발생했습니다.", e);
        }

        log.info("ScheduledMessage created: id={}, senderId={}, channelId={}, scheduledAt={}",
                saved.getId(), senderId, request.channelId(), request.scheduledAt());
        return ScheduledMessageResponse.from(saved);
    }

    @Override
    public void cancelScheduledMessage(String scheduledMessageId, String requesterId) {
        ScheduledMessage domain = scheduleRepository.findById(scheduledMessageId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.SCHEDULE_NOT_FOUND));

        // 본인 검증
        if (!domain.getSenderId().equals(requesterId)) {
            throw new ChatException(ChatErrorCode.SCHEDULE_CANCEL_FORBIDDEN);
        }

        // 도메인 취소 (PENDING 아니면 IllegalStateException)
        try {
            domain.cancel();
        } catch (IllegalStateException e) {
            throw new ChatException(ChatErrorCode.SCHEDULE_NOT_CANCELLABLE);
        }

        scheduleRepository.save(domain);

        // Quartz Job 취소
        try {
            quartzJobScheduler.unschedule(scheduledMessageId);
        } catch (SchedulerException e) {
            log.warn("Quartz unschedule failed for id={}", scheduledMessageId, e);
        }

        log.info("ScheduledMessage cancelled: id={}, requesterId={}", scheduledMessageId, requesterId);
    }

    @Override
    public void executeScheduledMessage(String scheduledMessageId) {
        ScheduledMessage domain = scheduleRepository.findById(scheduledMessageId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.SCHEDULE_NOT_FOUND));

        domain.markExecuting();
        scheduleRepository.save(domain);

        try {
            log.info("Executing scheduled message: id={}, channelId={}", scheduledMessageId, domain.getChannelId());

            messageSendService.sendScheduledMessage(
                    domain.getSenderId(),
                    domain.getChannelId(),
                    domain.getContent());

            domain.markExecuted();
            scheduleRepository.save(domain);
            log.info("ScheduledMessage executed: id={}", scheduledMessageId);
        } catch (Exception e) {
            log.error("ScheduledMessage execution failed: id={}", scheduledMessageId, e);
            domain.markFailed();
            scheduleRepository.save(domain);

            if (domain.isRetryable()) {
                ZonedDateTime retryAt = ZonedDateTime.now().plusSeconds(RETRY_DELAY_SECONDS);
                try {
                    quartzJobScheduler.scheduleRetry(scheduledMessageId, retryAt);
                    log.info("ScheduledMessage retry scheduled: id={}, retryAt={}", scheduledMessageId, retryAt);
                } catch (SchedulerException se) {
                    log.error("Failed to schedule retry for id={}", scheduledMessageId, se);
                }
            } else {
                log.warn("ScheduledMessage max retry exceeded, marking FAILED: id={}", scheduledMessageId);
            }
            throw e;
        }
    }

    // ── private helpers ───────────────────────────────────────────────

    private MessageContent buildContent(CreateScheduledMessageRequest request) {
        return switch (request.contentType().toUpperCase()) {
            case "TEXT" -> MessageContent.text(request.text());
            case "IMAGE" -> MessageContent.image(request.mediaUrl(), request.fileName(), request.fileSize());
            case "FILE" -> MessageContent.file(request.mediaUrl(), request.fileName(), request.fileSize(), request.mimeType());
            default -> throw new ChatException(ChatErrorCode.DOMAIN_RULE_VIOLATION);
        };
    }
}
