package com.example.chat.storage.domain.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 채팅방 메타데이터 JPA Entity.
 * createdAt / updatedAt 생명주기는 {@link BaseEntity} 에서 관리한다.
 * lastReadMessageId / lastReadAt 은 {@link LastReadPointer} 값 객체로 캡슐화한다.
 */
@Entity
@Table(name = "chat_channel_metadata", indexes = {
        @Index(name = "idx_metadata_user_id", columnList = "user_id"),
        @Index(name = "idx_metadata_channel_id", columnList = "channel_id"),
        @Index(name = "idx_user_activity", columnList = "user_id, last_activity_at DESC"),
        @Index(name = "idx_user_favorite", columnList = "user_id, favorite"),
        @Index(name = "idx_user_pinned", columnList = "user_id, pinned")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_channel_user", columnNames = { "channel_id", "user_id" })
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatChannelMetadataEntity extends BaseEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "channel_id", length = 36, nullable = false)
    private String channelId;

    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

    @Column(name = "notification_enabled", nullable = false)
    private boolean notificationEnabled = true;

    @Column(name = "favorite", nullable = false)
    private boolean favorite = false;

    @Column(name = "pinned", nullable = false)
    private boolean pinned = false;

    @Embedded
    private LastReadPointer lastRead = LastReadPointer.empty();

    @Column(name = "unread_count", nullable = false)
    private int unreadCount = 0;

    @Column(name = "last_activity_at")
    private Instant lastActivityAt;

    @PrePersist
    @Override
    protected void prePersist() {
        super.prePersist();
        if (lastActivityAt == null) {
            lastActivityAt = Instant.now();
        }
    }

    private ChatChannelMetadataEntity(String id, String channelId, String userId) {
        this.id = id;
        this.channelId = channelId;
        this.userId = userId;
        this.notificationEnabled = true;
        this.favorite = false;
        this.pinned = false;
        this.unreadCount = 0;
        this.lastRead = LastReadPointer.empty();
    }

    /**
     * 새 채널 메타데이터 엔티티를 생성하는 팩토리 메서드.
     */
    public static ChatChannelMetadataEntity create(String id, String channelId, String userId) {
        return new ChatChannelMetadataEntity(id, channelId, userId);
    }

    // =============================================
    // 위임 접근자 — 하위 호환성 유지
    // =============================================

    public String getLastReadMessageId() {
        return lastRead != null ? lastRead.getLastReadMessageId() : null;
    }

    public Instant getLastReadAt() {
        return lastRead != null ? lastRead.getLastReadAt() : null;
    }

    // =============================================
    // 비즈니스 메서드
    // =============================================

    public void toggleFavorite() {
        this.favorite = !this.favorite;
    }

    public void togglePin() {
        this.pinned = !this.pinned;
    }

    public void toggleNotification() {
        this.notificationEnabled = !this.notificationEnabled;
    }

    public void markAsRead(String messageId) {
        this.lastRead = lastRead.advance(messageId);
        this.unreadCount = 0;
    }

    public void incrementUnread() {
        this.unreadCount++;
    }

    public void recordActivity() {
        this.lastActivityAt = Instant.now();
    }
}
