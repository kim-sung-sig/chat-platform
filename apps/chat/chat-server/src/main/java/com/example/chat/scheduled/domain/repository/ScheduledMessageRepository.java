package com.example.chat.scheduled.domain.repository;

import com.example.chat.scheduled.domain.model.ScheduledMessage;
import com.example.chat.scheduled.domain.model.ScheduleStatus;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 예약 발송 저장소 Port (Domain Interface)
 */
public interface ScheduledMessageRepository {

    ScheduledMessage save(ScheduledMessage scheduledMessage);

    Optional<ScheduledMessage> findById(String id);

    List<ScheduledMessage> findByChannelIdAndSenderIdAndStatusAndScheduledAtAfter(
            String channelId,
            String senderId,
            ScheduleStatus status,
            ZonedDateTime since
    );

    List<ScheduledMessage> findByChannelIdAndSenderIdOrderByScheduledAtDesc(
            String channelId,
            String senderId
    );

    /**
     * 오늘 특정 채널·사용자의 PENDING 예약 수 조회 (한도 검증용)
     */
    long countByChannelIdAndSenderIdAndStatusAndScheduledAtBetween(
            String channelId,
            String senderId,
            ScheduleStatus status,
            ZonedDateTime from,
            ZonedDateTime to
    );
}
