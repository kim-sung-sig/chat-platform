package com.example.chat.system.repository;

import com.example.chat.system.domain.entity.ScheduleRule;
import com.example.chat.system.domain.enums.ScheduleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 스케줄 규칙 Repository
 */
@Repository
public interface ScheduleRuleRepository extends JpaRepository<ScheduleRule, Long> {

    /**
     * 메시지 ID로 스케줄 조회
     */
    List<ScheduleRule> findByMessageId(Long messageId);

    /**
     * 활성화된 스케줄 조회
     */
    List<ScheduleRule> findByIsActiveTrue();

    /**
     * Job 이름으로 조회
     */
    Optional<ScheduleRule> findByJobName(String jobName);

    /**
     * 스케줄 타입으로 조회
     */
    List<ScheduleRule> findByScheduleTypeAndIsActiveTrue(ScheduleType scheduleType);

    /**
     * 실행 대상 스케줄 조회
     */
    @Query("SELECT sr FROM ScheduleRule sr " +
           "WHERE sr.isActive = true " +
           "AND sr.nextExecutionTime <= :currentTime " +
           "AND (sr.maxExecutionCount IS NULL OR sr.executionCount < sr.maxExecutionCount)")
    List<ScheduleRule> findExecutableSchedules(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 메시지 ID와 활성 상태로 스케줄 조회
     */
    @Query("SELECT sr FROM ScheduleRule sr " +
           "WHERE sr.message.id = :messageId " +
           "AND sr.isActive = true")
    List<ScheduleRule> findActiveSchedulesByMessageId(@Param("messageId") Long messageId);
}