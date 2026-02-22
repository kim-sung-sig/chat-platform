package com.example.chat.domain.channel.metadata;

import java.time.Instant;

import com.example.chat.domain.channel.ChannelId;
import com.example.chat.domain.message.MessageId;
import com.example.chat.domain.service.DomainException;
import com.example.chat.domain.user.UserId;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 채팅방 메타데이터 Aggregate Root
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public class ChannelMetadata {
    private ChannelMetadataId id;
    private ChannelId channelId;
    private UserId userId;
    private Instant createdAt;

    // 사용자별 설정
    private boolean notificationEnabled;
    private boolean favorite;
    private boolean pinned;

    // 읽기 상태
    private MessageId lastReadMessageId;
    private Instant lastReadAt;
    private int unreadCount;

    // 메타 정보
    private Instant lastActivityAt;
    private Instant updatedAt;

    /**
     * 새로운 채팅방 메타데이터 생성
     */
    public static ChannelMetadata create(ChannelId channelId, UserId userId) {
        Instant now = Instant.now();
        return ChannelMetadata.builder()
                .id(ChannelMetadataId.generate())
                .channelId(channelId)
                .userId(userId)
                .createdAt(now)
                .notificationEnabled(true)
                .favorite(false)
                .pinned(false)
                .unreadCount(0)
                .lastActivityAt(now)
                .updatedAt(now)
                .build();
    }

    // === Business Methods ===

    /**
     * 메시지 읽음 처리
     */
    public void markAsRead(MessageId messageId) {
        Instant now = Instant.now();
        this.lastReadMessageId = messageId;
        this.lastReadAt = now;
        this.unreadCount = 0;
        this.lastActivityAt = now;
        this.updatedAt = now;
    }

    /**
     * 읽지 않은 메시지 수 설정
     */
    public void updateUnreadCount(int count) {
        if (count < 0) {
            throw new DomainException("Unread count cannot be negative");
        }

        this.unreadCount = count;
        this.lastActivityAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * 읽지 않은 메시지 수 증가
     */
    public void incrementUnreadCount() {
        this.unreadCount++;
        this.lastActivityAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * 읽지 않은 메시지 수 감소
     */
    public void decrementUnreadCount() {
        if (unreadCount > 0) {
            unreadCount--;
            updatedAt = Instant.now();
        }
    }

    /**
     * 알림 설정 변경
     */
    public void updateNotificationEnabled(boolean enabled) {
        this.notificationEnabled = enabled;
        updatedAt = Instant.now();
    }

    /**
     * 알림 토글
     */
    public void toggleNotification() {
        this.notificationEnabled = !notificationEnabled;
        updatedAt = Instant.now();
    }

    /**
     * 즐겨찾기 설정 변경
     */
    public void updateFavorite(boolean favorite) {
        this.favorite = favorite;
        updatedAt = Instant.now();
    }

    /**
     * 즐겨찾기 토글
     */
    public void toggleFavorite() {
        this.favorite = !favorite;
        updatedAt = Instant.now();
    }

    /**
     * 상단 고정 설정 변경
     */
    public void updatePinned(boolean pinned) {
        this.pinned = pinned;
        updatedAt = Instant.now();
    }

    /**
     * 상단 고정 토글
     */
    public void togglePinned() {
        this.pinned = !pinned;
        updatedAt = Instant.now();
    }

    /**
     * 마지막 활동 시간 업데이트
     */
    public void updateLastActivity() {
        this.lastActivityAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    // === Query Methods ===

    /**
     * 읽지 않은 메시지가 있는지 확인
     */
    public boolean hasUnreadMessages() {
        return unreadCount > 0;
    }

    /**
     * Storage Layer에서 재구성
     */
    public static ChannelMetadata fromStorage(
            ChannelMetadataId id,
            ChannelId channelId,
            UserId userId,
            Instant createdAt,
            boolean notificationEnabled,
            boolean favorite,
            boolean pinned,
            MessageId lastReadMessageId,
            Instant lastReadAt,
            int unreadCount,
            Instant lastActivityAt,
            Instant updatedAt) {
        return ChannelMetadata.builder()
                .id(id)
                .channelId(channelId)
                .userId(userId)
                .createdAt(createdAt)
                .notificationEnabled(notificationEnabled)
                .favorite(favorite)
                .pinned(pinned)
                .lastReadMessageId(lastReadMessageId)
                .lastReadAt(lastReadAt)
                .unreadCount(unreadCount)
                .lastActivityAt(lastActivityAt)
                .updatedAt(updatedAt)
                .build();
    }
}
