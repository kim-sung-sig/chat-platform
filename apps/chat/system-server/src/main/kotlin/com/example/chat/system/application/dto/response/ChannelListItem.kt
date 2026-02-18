package com.example.chat.system.application.dto.response

import com.example.chat.domain.channel.ChannelType
import java.time.Instant

/**
 * 채팅방 목록 아이템 DTO
 *
 * UI에 필요한 모든 정보를 통합하여 제공
 */
data class ChannelListItem(
	// === 채널 기본 정보 ===
	val channelId: String,
	val channelName: String,
	val channelDescription: String?,
	val channelType: ChannelType,
	val active: Boolean,

	// === 마지막 메시지 정보 ===
	val lastMessageId: String?,
	val lastMessageContent: String?,
	val lastMessageSenderId: String?,
	val lastMessageSenderName: String?,
	val lastMessageTime: Instant?,

	// === 사용자별 메타 정보 (ChannelMetadata) ===
	val unreadCount: Int,
	val favorite: Boolean,
	val pinned: Boolean,
	val notificationEnabled: Boolean,
	val lastReadAt: Instant?,
	val lastActivityAt: Instant?,

	// === 멤버 정보 ===
	val memberCount: Int,

	// === 1:1 채팅 전용 (DIRECT 타입) ===
	val otherUserId: String?,
	val otherUserName: String?,
	val otherUserEmail: String?,

	// === 그룹 채팅 전용 (GROUP 타입) ===
	val ownerUserId: String?,
	val ownerUserName: String?,

	// === 시간 정보 ===
	val createdAt: Instant
)
