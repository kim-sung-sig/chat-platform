package com.example.chat.domain.channel.metadata

import com.example.chat.domain.channel.ChannelId
import com.example.chat.domain.user.UserId

/**
 * 채팅방 메타데이터 Repository 인터페이스 (포트)
 *
 * Hexagonal Architecture의 Port
 */
interface ChannelMetadataRepository {

	/**
	 * 메타데이터 저장
	 */
	fun save(metadata: ChannelMetadata): ChannelMetadata

	/**
	 * ID로 메타데이터 조회
	 */
	fun findById(id: ChannelMetadataId): ChannelMetadata?

	/**
	 * 채널 ID와 사용자 ID로 조회
	 */
	fun findByChannelIdAndUserId(channelId: ChannelId, userId: UserId): ChannelMetadata?

	/**
	 * 사용자의 모든 채팅방 메타데이터 조회
	 */
	fun findByUserId(userId: UserId): List<ChannelMetadata>

	/**
	 * 여러 채널의 메타데이터 배치 조회
	 */
	fun findByChannelIdsAndUserId(channelIds: List<ChannelId>, userId: UserId): Map<ChannelId, ChannelMetadata>

	/**
	 * 즐겨찾기 채팅방 메타데이터 조회
	 */
	fun findFavoritesByUserId(userId: UserId): List<ChannelMetadata>

	/**
	 * 상단 고정 채팅방 메타데이터 조회
	 */
	fun findPinnedByUserId(userId: UserId): List<ChannelMetadata>

	/**
	 * 읽지 않은 메시지가 있는 채팅방 메타데이터 조회
	 */
	fun findWithUnreadByUserId(userId: UserId): List<ChannelMetadata>

	/**
	 * 메타데이터 삭제
	 */
	fun deleteById(id: ChannelMetadataId)

	/**
	 * 채널의 모든 메타데이터 삭제 (채널 삭제 시)
	 */
	fun deleteByChannelId(channelId: ChannelId)

	/**
	 * 메타데이터 존재 여부 확인
	 */
	fun existsByChannelIdAndUserId(channelId: ChannelId, userId: UserId): Boolean
}
