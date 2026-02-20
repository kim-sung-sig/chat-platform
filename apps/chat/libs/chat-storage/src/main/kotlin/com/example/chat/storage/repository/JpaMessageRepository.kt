package com.example.chat.storage.repository

import com.example.chat.storage.entity.ChatMessageEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

/**
 * 메시지 JPA Repository
 */
interface JpaMessageRepository : JpaRepository<ChatMessageEntity, String> {

    /**
     * 채널 ID로 메시지 목록 조회 (생성 시간 역순)
     */
    fun findByChannelIdOrderByCreatedAtDesc(channelId: String): List<ChatMessageEntity>

    /**
     * 채널 ID별 마지막 메시지 조회 (배치)
     */
    @Query("""
        SELECT m FROM ChatMessageEntity m 
        WHERE m.channelId IN :channelIds 
        AND m.createdAt = (
            SELECT MAX(m2.createdAt) FROM ChatMessageEntity m2 WHERE m2.channelId = m.channelId
        )
    """)
    fun findLastMessagesByChannelIds(channelIds: List<String>): List<ChatMessageEntity>

    /**
     * 발신자 ID로 메시지 목록 조회
     */
    fun findBySenderIdOrderByCreatedAtDesc(senderId: String): List<ChatMessageEntity>
}
