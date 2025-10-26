package com.example.chat.system.repository;

import com.example.chat.system.domain.entity.MessageHistory;
import com.example.chat.system.domain.enums.PublishStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 메시지 발행 이력 Repository
 * 커서 기반 페이징 지원
 */
@Repository
public interface MessageHistoryRepository extends JpaRepository<MessageHistory, Long> {

    /**
     * 메시지 ID로 발행 이력 조회 (커서 기반 페이징)
     */
    @Query("SELECT mh FROM MessageHistory mh " +
           "WHERE mh.message.id = :messageId " +
           "AND (:cursor IS NULL OR mh.id < :cursor) " +
           "ORDER BY mh.id DESC")
    List<MessageHistory> findByMessageIdWithCursor(
        @Param("messageId") Long messageId,
        @Param("cursor") Long cursor,
        Pageable pageable
    );

    /**
     * 고객 ID로 발행 이력 조회 (커서 기반 페이징)
     */
    @Query("SELECT mh FROM MessageHistory mh " +
           "WHERE mh.customer.id = :customerId " +
           "AND (:cursor IS NULL OR mh.id < :cursor) " +
           "ORDER BY mh.id DESC")
    List<MessageHistory> findByCustomerIdWithCursor(
        @Param("customerId") Long customerId,
        @Param("cursor") Long cursor,
        Pageable pageable
    );

    /**
     * 발행 상태로 조회
     */
    List<MessageHistory> findByPublishStatus(PublishStatus publishStatus);

    /**
     * 재시도 대상 조회
     */
    @Query("SELECT mh FROM MessageHistory mh " +
           "WHERE mh.publishStatus = 'RETRY' " +
           "AND mh.retryCount < :maxRetryCount " +
           "ORDER BY mh.createdAt ASC")
    List<MessageHistory> findRetryTargets(@Param("maxRetryCount") int maxRetryCount);

    /**
     * 기간별 발행 이력 통계
     */
    @Query("SELECT mh.publishStatus, COUNT(mh) FROM MessageHistory mh " +
           "WHERE mh.publishedAt BETWEEN :startDate AND :endDate " +
           "GROUP BY mh.publishStatus")
    List<Object[]> getPublishStatistics(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * 메시지별 발행 성공률 조회
     */
    @Query("SELECT mh.message.id, " +
           "SUM(CASE WHEN mh.publishStatus = 'SUCCESS' THEN 1 ELSE 0 END) as successCount, " +
           "COUNT(mh) as totalCount " +
           "FROM MessageHistory mh " +
           "WHERE mh.message.id = :messageId " +
           "GROUP BY mh.message.id")
    Object[] getPublishSuccessRate(@Param("messageId") Long messageId);

    /**
     * 스케줄 규칙별 발행 이력 조회 (커서 기반)
     */
    @Query("SELECT mh FROM MessageHistory mh " +
           "WHERE mh.scheduleRuleId = :scheduleRuleId " +
           "AND (:cursor IS NULL OR mh.id < :cursor) " +
           "ORDER BY mh.id DESC")
    List<MessageHistory> findByScheduleRuleIdWithCursor(
        @Param("scheduleRuleId") Long scheduleRuleId,
        @Param("cursor") Long cursor,
        Pageable pageable
    );
}