package com.example.chat.storage.repository;

import com.example.chat.storage.entity.ChatMessageEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface JpaChatMessageRepository extends JpaRepository<ChatMessageEntity, String> {

    /**
     * 커서 기반 페이징 - 특정 시간 이후의 메시지 조회
     */
    @Query("SELECT m FROM ChatMessageEntity m WHERE m.channelId = :channelId " +
           "AND m.createdAt < :cursorTime " +
           "ORDER BY m.createdAt DESC")
    Page<ChatMessageEntity> findByChannelIdWithCursor(
        @Param("channelId") String channelId,
        @Param("cursorTime") Instant cursorTime,
        Pageable pageable
    );

    /**
     * 커서 없이 최신 메시지 조회
     */
    @Query("SELECT m FROM ChatMessageEntity m WHERE m.channelId = :channelId " +
           "ORDER BY m.createdAt DESC")
    Page<ChatMessageEntity> findByChannelIdOrderByCreatedAtDesc(
        @Param("channelId") String channelId,
        Pageable pageable
    );

    /**
     * 특정 사용자가 보낸 메시지 조회
     */
    List<ChatMessageEntity> findBySenderIdOrderByCreatedAtDesc(String senderId);
}
