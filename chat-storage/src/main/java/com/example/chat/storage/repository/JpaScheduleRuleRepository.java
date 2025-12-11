package com.example.chat.storage.repository;

import com.example.chat.domain.schedule.ScheduleStatus;
import com.example.chat.domain.schedule.ScheduleType;
import com.example.chat.storage.entity.ScheduleRuleEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaScheduleRuleRepository extends JpaRepository<ScheduleRuleEntity, String> {

    /**
     * 비관적 락으로 조회 (동시성 제어)
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM ScheduleRuleEntity s WHERE s.id = :id")
    Optional<ScheduleRuleEntity> findByIdWithLock(@Param("id") String id);

    /**
     * 실행 가능한 단발성 스케줄 목록 조회
     */
    @Query("SELECT s FROM ScheduleRuleEntity s WHERE s.scheduleType = :type " +
           "AND (s.scheduleStatus = 'PENDING' OR s.scheduleStatus = 'ACTIVE')")
    List<ScheduleRuleEntity> findExecutableSchedulesByType(@Param("type") ScheduleType type);

    /**
     * 활성화된 주기적 스케줄 목록 조회
     */
    List<ScheduleRuleEntity> findByScheduleTypeAndScheduleStatus(ScheduleType type, ScheduleStatus status);

    /**
     * 사용자의 활성 스케줄 목록 조회
     */
    List<ScheduleRuleEntity> findBySenderIdAndScheduleStatusIn(String senderId, List<ScheduleStatus> statuses);

    /**
     * 채널의 활성 스케줄 목록 조회
     */
    List<ScheduleRuleEntity> findByChannelIdAndScheduleStatusIn(String channelId, List<ScheduleStatus> statuses);
}
