package com.example.chat.system.application.dto.response;

import java.time.Instant;

import com.example.chat.domain.channel.ChannelType;

/**
 * 채팅방 목록 아이템 DTO
 *
 * UI에 필요한 모든 정보를 통합하여 제공
 */
public record ChannelListItem(
        // === 채널 기본 정보 ===
        String channelId,
        String channelName,
        String channelDescription,
        ChannelType channelType,
        boolean active,

        // === 마지막 메시지 정보 ===
        String lastMessageId,
        String lastMessageContent,
        String lastMessageSenderId,
        String lastMessageSenderName,
        Instant lastMessageTime,

        // === 사용자별 메타 정보 (ChannelMetadata) ===
        int unreadCount,
        boolean favorite,
        boolean pinned,
        boolean notificationEnabled,
        Instant lastReadAt,
        Instant lastActivityAt,

        // === 멤버 정보 ===
        int memberCount,

        // === 1:1 채팅 전용 (DIRECT 타입) ===
        String otherUserId,
        String otherUserName,
        String otherUserEmail,

        // === 그룹 채팅 전용 (GROUP 타입) ===
        String ownerUserId,
        String ownerUserName,

        // === 시간 정보 ===
        Instant createdAt) {
}
