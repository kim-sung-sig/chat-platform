package com.example.chat.scheduled.infrastructure.datasource;

import com.example.chat.scheduled.domain.model.ScheduleStatus;
import com.example.chat.scheduled.domain.model.ScheduledMessage;
import com.example.chat.scheduled.domain.repository.ScheduledMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

/**
 * ScheduledMessageRepository Port 구현체 (JPA Adapter)
 *
 * 도메인 ↔ Entity 변환을 담당한다.
 */
@Repository
@RequiredArgsConstructor
public class ScheduledMessageRepositoryAdapter implements ScheduledMessageRepository {

    private final JpaScheduledMessageRepository jpa;

    @Override
    public ScheduledMessage save(ScheduledMessage domain) {
        ScheduledMessageEntity saved = jpa.save(ScheduledMessageEntity.fromDomain(domain));
        return saved.toDomain();
    }

    @Override
    public Optional<ScheduledMessage> findById(String id) {
        return jpa.findById(id).map(ScheduledMessageEntity::toDomain);
    }

    @Override
    public List<ScheduledMessage> findByChannelIdAndSenderIdAndStatusAndScheduledAtAfter(
            String channelId, String senderId, ScheduleStatus status, ZonedDateTime since) {
        return jpa.findByChannelIdAndSenderIdAndScheduleStatusAndScheduledAtAfter(
                        channelId, senderId, status, toLocal(since))
                .stream().map(ScheduledMessageEntity::toDomain).toList();
    }

    @Override
    public List<ScheduledMessage> findByChannelIdAndSenderIdOrderByScheduledAtDesc(
            String channelId, String senderId) {
        return jpa.findByChannelIdAndSenderIdOrderByScheduledAtDesc(channelId, senderId)
                .stream().map(ScheduledMessageEntity::toDomain).toList();
    }

    @Override
    public long countByChannelIdAndSenderIdAndStatusAndScheduledAtBetween(
            String channelId, String senderId, ScheduleStatus status,
            ZonedDateTime from, ZonedDateTime to) {
        return jpa.countByChannelIdAndSenderIdAndScheduleStatusAndScheduledAtBetween(
                channelId, senderId, status, toLocal(from), toLocal(to));
    }

    private static LocalDateTime toLocal(ZonedDateTime zdt) {
        return zdt == null ? null : zdt.withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime();
    }
}
