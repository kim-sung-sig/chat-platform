package com.example.chat.storage.adapter;

import com.example.chat.domain.schedule.ScheduleId;
import com.example.chat.domain.schedule.ScheduleRule;
import com.example.chat.domain.schedule.ScheduleRuleRepository;
import com.example.chat.domain.schedule.ScheduleStatus;
import com.example.chat.domain.schedule.ScheduleType;
import com.example.chat.storage.entity.ScheduleRuleEntity;
import com.example.chat.storage.mapper.ScheduleMapper;
import com.example.chat.storage.repository.JpaScheduleRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ScheduleRuleRepository 구현체 (Adapter)
 */
@Repository
@RequiredArgsConstructor
public class ScheduleRepositoryAdapter implements ScheduleRuleRepository {

    private final JpaScheduleRuleRepository jpaRepository;
    private final ScheduleMapper mapper;

    @Override
    @Transactional
    public ScheduleRule save(ScheduleRule scheduleRule) {
        ScheduleRuleEntity entity = mapper.toEntity(scheduleRule);
        ScheduleRuleEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ScheduleRule> findById(ScheduleId id) {
        return jpaRepository.findById(id.getValue())
                .map(mapper::toDomain);
    }

    @Override
    @Transactional
    public Optional<ScheduleRule> findByIdWithLock(ScheduleId id) {
        return jpaRepository.findByIdWithLock(id.getValue())
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScheduleRule> findExecutableOneTimeSchedules() {
        List<ScheduleRuleEntity> entities = jpaRepository.findExecutableSchedulesByType(ScheduleType.ONE_TIME);
        return entities.stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScheduleRule> findActiveRecurringSchedules() {
        List<ScheduleRuleEntity> entities = jpaRepository.findByScheduleTypeAndScheduleStatus(
                ScheduleType.RECURRING,
                ScheduleStatus.ACTIVE
        );
        return entities.stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScheduleRule> findActiveBySenderId(String senderId) {
        List<ScheduleRuleEntity> entities = jpaRepository.findBySenderIdAndScheduleStatusIn(
                senderId,
                List.of(ScheduleStatus.PENDING, ScheduleStatus.ACTIVE)
        );
        return entities.stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScheduleRule> findActiveByChannelId(String channelId) {
        List<ScheduleRuleEntity> entities = jpaRepository.findByChannelIdAndScheduleStatusIn(
                channelId,
                List.of(ScheduleStatus.PENDING, ScheduleStatus.ACTIVE)
        );
        return entities.stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(ScheduleId id) {
        jpaRepository.deleteById(id.getValue());
    }
}
