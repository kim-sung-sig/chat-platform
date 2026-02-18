package com.example.chat.storage.repository

import com.example.chat.storage.entity.ChatChannelMetadataEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

/**
 * 채팅방 메타데이터 JPA Repository
 */
interface JpaChannelMetadataRepository : JpaRepository<ChatChannelMetadataEntity, String> {

	/**
	 * 채널 ID와 사용자 ID로 조회
	 */
	fun findByChannelIdAndUserId(channelId: String, userId: String): ChatChannelMetadataEntity?

	/**
	 * 사용자의 모든 메타데이터 조회
	 */
	fun findByUserId(userId: String): List<ChatChannelMetadataEntity>

	/**
	 * 여러 채널의 메타데이터 배치 조회
	 */
	@Query("SELECT m FROM ChatChannelMetadataEntity m WHERE m.channelId IN :channelIds AND m.userId = :userId")
	fun findByChannelIdsAndUserId(channelIds: List<String>, userId: String): List<ChatChannelMetadataEntity>

	/**
	 * 즐겨찾기 메타데이터 조회
	 */
	@Query("SELECT m FROM ChatChannelMetadataEntity m WHERE m.userId = :userId AND m.favorite = true ORDER BY m.lastActivityAt DESC")
	fun findFavoritesByUserId(userId: String): List<ChatChannelMetadataEntity>

	/**
	 * 상단 고정 메타데이터 조회
	 */
	@Query("SELECT m FROM ChatChannelMetadataEntity m WHERE m.userId = :userId AND m.pinned = true ORDER BY m.lastActivityAt DESC")
	fun findPinnedByUserId(userId: String): List<ChatChannelMetadataEntity>

	/**
	 * 읽지 않은 메시지가 있는 메타데이터 조회
	 */
	@Query("SELECT m FROM ChatChannelMetadataEntity m WHERE m.userId = :userId AND m.unreadCount > 0 ORDER BY m.lastActivityAt DESC")
	fun findWithUnreadByUserId(userId: String): List<ChatChannelMetadataEntity>

	/**
	 * 채널의 모든 메타데이터 삭제
	 */
	fun deleteByChannelId(channelId: String)

	/**
	 * 존재 여부 확인
	 */
	fun existsByChannelIdAndUserId(channelId: String, userId: String): Boolean
}
