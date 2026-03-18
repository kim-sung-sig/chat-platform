package com.example.chat.storage.domain.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.chat.storage.domain.entity.ChatMessageEntity;

/**
 * 메시지 JPA Repository
 */
@Repository
public interface JpaMessageRepository extends JpaRepository<ChatMessageEntity, String> {

    /**
     * Cursor 기반 메시지 조회 — cursor(created_at) 이전 메시지를 최신순으로 조회
     */
    List<ChatMessageEntity> findByChannelIdAndCreatedAtBeforeOrderByCreatedAtDesc(
            String channelId, Instant createdAt, Pageable pageable);

    /**
     * Cursor 없이 첫 페이지 조회 (최신순)
     */
    List<ChatMessageEntity> findByChannelIdOrderByCreatedAtDesc(
            String channelId, Pageable pageable);

    /**
     * 발신자 ID + Cursor 기반 조회
     */
    List<ChatMessageEntity> findBySenderIdAndCreatedAtBeforeOrderByCreatedAtDesc(
            String senderId, Instant createdAt, Pageable pageable);

    /**
     * 발신자 ID 첫 페이지 조회 (최신순)
     */
    List<ChatMessageEntity> findBySenderIdOrderByCreatedAtDesc(
            String senderId, Pageable pageable);

    /**
     * [APPROVED @Query EXCEPTION]
     * Reason: correlated subquery (SELECT MAX(m2.createdAt) ... GROUP BY channelId)
     * cannot be expressed as a Spring Data named method.
     */
    @Query("SELECT m FROM ChatMessageEntity m " +
            "WHERE m.channelId IN :channelIds " +
            "AND m.createdAt = (" +
            "    SELECT MAX(m2.createdAt) FROM ChatMessageEntity m2 WHERE m2.channelId = m.channelId" +
            ")")
    List<ChatMessageEntity> findLastMessagesByChannelIds(@Param("channelIds") List<String> channelIds);

    /**
     * 읽음 처리 시 커서 이전(포함) 메시지의 unread_count 일괄 -1 감소.
     * MAX(0, unread_count - 1) 보장: CASE WHEN 사용 (JPQL은 GREATEST 미지원).
     * Phase 6: Kafka Consumer 에서 호출 (비동기 배치 처리).
     */
    @Modifying
    @Query("UPDATE ChatMessageEntity m " +
            "SET m.unreadCount = CASE WHEN m.unreadCount > 0 THEN m.unreadCount - 1 ELSE 0 END " +
            "WHERE m.channelId = :channelId AND m.createdAt <= :cursor")
    int bulkDecrementUnreadCountBeforeCursor(
            @Param("channelId") String channelId,
            @Param("cursor") Instant cursor);

    /**
     * 멤버 퇴장 시 해당 유저가 읽지 않은 메시지(cursor 이후)의 unread_count 일괄 -1 감소.
     * lastReadAt이 null이면 채널 전체 메시지 감소.
     * Phase 8: MemberLeftKafkaConsumer 에서 호출.
     */
    @Modifying
    @Query("UPDATE ChatMessageEntity m " +
            "SET m.unreadCount = CASE WHEN m.unreadCount > 0 THEN m.unreadCount - 1 ELSE 0 END " +
            "WHERE m.channelId = :channelId AND (:lastReadAt IS NULL OR m.createdAt > :lastReadAt)")
    int bulkDecrementUnreadCountAfterCursor(
            @Param("channelId") String channelId,
            @Param("lastReadAt") Instant lastReadAt);
}
