package com.example.chat.storage.repository;

import com.example.chat.storage.entity.ChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 메시지 JPA Repository
 */
public interface JpaMessageRepository extends JpaRepository<ChatMessageEntity, String> {

    /**
     * 채널 ID로 메시지 목록 조회 (생성 시간 역순)
     */
    List<ChatMessageEntity> findByChannelIdOrderByCreatedAtDesc(String channelId);

    /**
     * 채널 ID별 마지막 메시지 조회 (배치)
     */
    @Query("SELECT m FROM ChatMessageEntity m " +
            "WHERE m.channelId IN :channelIds " +
            "AND m.createdAt = (" +
            "    SELECT MAX(m2.createdAt) FROM ChatMessageEntity m2 WHERE m2.channelId = m.channelId" +
            ")")
    List<ChatMessageEntity> findLastMessagesByChannelIds(@Param("channelIds") List<String> channelIds);

    /**
     * 발신자 ID로 메시지 목록 조회
     */
    List<ChatMessageEntity> findBySenderIdOrderByCreatedAtDesc(String senderId);
}
