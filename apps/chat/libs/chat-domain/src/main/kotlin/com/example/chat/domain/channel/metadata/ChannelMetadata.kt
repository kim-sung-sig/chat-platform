package com.example.chat.domain.channel.metadata

import com.example.chat.domain.channel.ChannelId
import com.example.chat.domain.message.MessageId
import com.example.chat.domain.service.DomainException
import com.example.chat.domain.user.UserId
import java.time.Instant

/**
 * 채팅방 메타데이터 Aggregate Root
 *
 * 책임:
 * - 사용자별 채팅방 설정 (알림, 즐겨찾기, 상단 고정)
 * - 읽기 상태 추적 (마지막 읽은 메시지, 읽지 않은 메시지 수)
 * - 마지막 활동 시간 관리
 *
 * CQRS 패턴:
 * - Command: Channel Aggregate (채널 생성/수정)
 * - Query: ChannelMetadata (사용자별 읽기 최적화)
 */
data class ChannelMetadata(
	val id: ChannelMetadataId,
	val channelId: ChannelId,
	val userId: UserId,
	val createdAt: Instant,

	// 사용자별 설정
	var notificationEnabled: Boolean = true,
	var favorite: Boolean = false,
	var pinned: Boolean = false,

	// 읽기 상태
	var lastReadMessageId: MessageId? = null,
	var lastReadAt: Instant? = null,
	var unreadCount: Int = 0,

	// 메타 정보
	var lastActivityAt: Instant,
	var updatedAt: Instant
) {
	companion object {
		/**
		 * 새로운 채팅방 메타데이터 생성
		 */
		fun create(channelId: ChannelId, userId: UserId): ChannelMetadata {
			val now = Instant.now()
			return ChannelMetadata(
				id = ChannelMetadataId.generate(),
				channelId = channelId,
				userId = userId,
				notificationEnabled = true,
				favorite = false,
				pinned = false,
				lastReadMessageId = null,
				lastReadAt = null,
				unreadCount = 0,
				lastActivityAt = now,
				createdAt = now,
				updatedAt = now
			)
		}
	}

	// === Business Methods ===

	/**
	 * 메시지 읽음 처리
	 */
	fun markAsRead(messageId: MessageId) {
		val now = Instant.now()
		lastReadMessageId = messageId
		lastReadAt = now
		unreadCount = 0
		lastActivityAt = now
		updatedAt = now
	}

	/**
	 * 읽지 않은 메시지 수 설정
	 */
	fun setUnreadCount(count: Int) {
		require(count >= 0) { throw DomainException("Unread count cannot be negative") }

		unreadCount = count
		lastActivityAt = Instant.now()
		updatedAt = Instant.now()
	}

	/**
	 * 읽지 않은 메시지 수 증가
	 */
	fun incrementUnreadCount() {
		unreadCount++
		lastActivityAt = Instant.now()
		updatedAt = Instant.now()
	}

	/**
	 * 읽지 않은 메시지 수 감소
	 */
	fun decrementUnreadCount() {
		if (unreadCount > 0) {
			unreadCount--
			updatedAt = Instant.now()
		}
	}

	/**
	 * 알림 설정 변경
	 */
	fun setNotificationEnabled(enabled: Boolean) {
		notificationEnabled = enabled
		updatedAt = Instant.now()
	}

	/**
	 * 알림 토글
	 */
	fun toggleNotification() {
		notificationEnabled = !notificationEnabled
		updatedAt = Instant.now()
	}

	/**
	 * 즐겨찾기 설정 변경
	 */
	fun setFavorite(favorite: Boolean) {
		this.favorite = favorite
		updatedAt = Instant.now()
	}

	/**
	 * 즐겨찾기 토글
	 */
	fun toggleFavorite() {
		favorite = !favorite
		updatedAt = Instant.now()
	}

	/**
	 * 상단 고정 설정 변경
	 */
	fun setPinned(pinned: Boolean) {
		this.pinned = pinned
		updatedAt = Instant.now()
	}

	/**
	 * 상단 고정 토글
	 */
	fun togglePinned() {
		pinned = !pinned
		updatedAt = Instant.now()
	}

	/**
	 * 마지막 활동 시간 업데이트
	 */
	fun updateLastActivity() {
		lastActivityAt = Instant.now()
		updatedAt = Instant.now()
	}

	// === Query Methods ===

	/**
	 * 읽지 않은 메시지가 있는지 확인
	 */
	fun hasUnreadMessages(): Boolean = unreadCount > 0

	/**
	 * 알림이 활성화되어 있는지 확인
	 */
	fun isNotificationEnabled(): Boolean = notificationEnabled

	/**
	 * 즐겨찾기 여부 확인
	 */
	fun isFavorite(): Boolean = favorite

	/**
	 * 상단 고정 여부 확인
	 */
	fun isPinned(): Boolean = pinned
}
