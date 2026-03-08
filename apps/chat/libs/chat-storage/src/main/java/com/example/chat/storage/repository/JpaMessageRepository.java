package com.example.chat.storage.repository;

import com.example.chat.storage.entity.ChatMessageEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * 메시지 JPA Repository
 */
@Repository
public interface JpaMessageRepository extends JpaRepository<ChatMessageEntity, String> {

    /**
     * Cursor 기반 메시지 조회 - cursor(created_at) 이전 메시지를 최신순으로 조회
     */
    @Query("SELECT m FROM ChatMessageEntity m " +
            "WHERE m.channelId = :channelId " +
            "AND m.createdAt < :cursor " +
            "ORDER BY m.createdAt DESC")
    List<ChatMessageEntity> findByChannelIdBeforeCursor(
            @Param("channelId") String channelId,
            @Param("cursor") Instant cursor,
            Pageable pageable);

    /**
     * Cursor 없이 첫 페이지 조회 (최신순)
     */
    @Query("SELECT m FROM ChatMessageEntity m " +
            "WHERE m.channelId = :channelId " +
            "ORDER BY m.createdAt DESC")
    List<ChatMessageEntity> findByChannelIdLatest(
            @Param("channelId") String channelId,
            Pageable pageable);

    /**
     * 발신자 ID + Cursor 기반 조회
     */
    @Query("SELECT m FROM ChatMessageEntity m " +
            "WHERE m.senderId = :senderId " +
            "AND m.createdAt < :cursor " +
            "ORDER BY m.createdAt DESC")
    List<ChatMessageEntity> findBySenderIdBeforeCursor(
            @Param("senderId") String senderId,
            @Param("cursor") Instant cursor,
            Pageable pageable);

    /**
     * 발신자 ID 첫 페이지 조회 (최신순)
     */
    @Query("SELECT m FROM ChatMessageEntity m " +
            "WHERE m.senderId = :senderId " +
            "ORDER BY m.createdAt DESC")
    List<ChatMessageEntity> findBySenderIdLatest(
            @Param("senderId") String senderId,
            Pageable pageable);

    /**
     * 채널 ID별 마지막 메시지 조회 (배치)
     */
    @Query("SELECT m FROM ChatMessageEntity m " +
            "WHERE m.channelId IN :channelIds " +
            "AND m.createdAt = (" +
            "    SELECT MAX(m2.createdAt) FROM ChatMessageEntity m2 WHERE m2.channelId = m.channelId" +
            ")")
    List<ChatMessageEntity> findLastMessagesByChannelIds(@Param("channelIds") List<String> channelIds);
}
