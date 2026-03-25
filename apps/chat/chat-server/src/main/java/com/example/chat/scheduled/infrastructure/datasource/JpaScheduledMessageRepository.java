package com.example.chat.scheduled.infrastructure.datasource;

import com.example.chat.scheduled.domain.model.ScheduleStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Spring Data JPA Repository for schedule_rules
 *
 * 직접 노출하지 않고 ScheduledMessageRepositoryAdapter에서만 사용.
 */
interface JpaScheduledMessageRepository extends JpaRepository<ScheduledMessageEntity, String> {

    List<ScheduledMessageEntity> findByChannelIdAndSenderIdAndScheduleStatusAndScheduledAtAfter(
            String channelId,
            String senderId,
            ScheduleStatus scheduleStatus,
            LocalDateTime since
    );

    List<ScheduledMessageEntity> findByChannelIdAndSenderIdOrderByScheduledAtDesc(
            String channelId,
            String senderId
    );

    long countByChannelIdAndSenderIdAndScheduleStatusAndScheduledAtBetween(
            String channelId,
            String senderId,
            ScheduleStatus scheduleStatus,
            LocalDateTime from,
            LocalDateTime to
    );
}
