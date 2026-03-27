package com.example.chat.storage.domain.entity;

import java.time.Instant;

import com.example.chat.common.core.enums.MessageStatus;
import com.example.chat.common.core.enums.MessageType;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 메시지 JPA Entity.
 * <p>
 * 이 엔티티는 BaseEntity 를 상속하지 않는다.
 * 이유: 메시지는 불변(write-once) 에 가깝고 updated_at 컬럼이 DDL 에 없다.
 * BaseEntity 를 상속하면 기존 스키마에 없는 updated_at 컬럼이 추가되므로
 * DDL 변경 없이 배포하기 위해 독립 선언을 유지한다.
 * 이 결정은 jpa-storage-refactor_plan.md FR-1a 및 R-4 에 문서화되어 있다.
 * </p>
 * 메시지 콘텐츠 필드(content_*)는 {@link MessageContent} 값 객체로 캡슐화한다.
 */
@Entity
@Table(name = "chat_messages", indexes = {
        @Index(name = "idx_chat_message_channel_created", columnList = "channel_id, created_at"),
        @Index(name = "idx_chat_message_sender", columnList = "sender_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessageEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "channel_id", nullable = false, length = 36)
    private String channelId;

    @Column(name = "sender_id", nullable = false, length = 36)
    private String senderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 20)
    private MessageType messageType;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_status", nullable = false, length = 20)
    private MessageStatus messageStatus = MessageStatus.PENDING;

    @Embedded
    private MessageContent content;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Column(name = "read_at")
    private Instant readAt;

    /**
     * 이 메시지를 아직 읽지 않은 멤버 수.
     * 발송 시 memberCount-1 로 초기화, 읽을 때마다 -1 (Kafka 비동기).
     * KakaoTalk '1' 표시에 사용되는 필드.
     */
    @Column(name = "unread_count", nullable = false)
    private int unreadCount = 0;

    @PrePersist
    protected void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    private ChatMessageEntity(String id, String channelId, String senderId,
                               MessageType messageType, MessageContent content) {
        this.id = id;
        this.channelId = channelId;
        this.senderId = senderId;
        this.messageType = messageType;
        this.messageStatus = MessageStatus.PENDING;
        this.content = content;
        this.unreadCount = 0;
    }

    /**
     * 새 메시지 엔티티를 생성하는 팩토리 메서드.
     */
    public static ChatMessageEntity create(String id, String channelId, String senderId,
                                           MessageType messageType, MessageContent content) {
        return new ChatMessageEntity(id, channelId, senderId, messageType, content);
    }

    // =============================================
    // 위임 접근자 — 하위 호환성 유지
    // =============================================

    public String getContentText() {
        return content != null ? content.getContentText() : null;
    }

    public String getContentMediaUrl() {
        return content != null ? content.getContentMediaUrl() : null;
    }

    public String getContentFileName() {
        return content != null ? content.getContentFileName() : null;
    }

    public Long getContentFileSize() {
        return content != null ? content.getContentFileSize() : null;
    }

    public String getContentMimeType() {
        return content != null ? content.getContentMimeType() : null;
    }

    // =============================================
    // 비즈니스 메서드 - 상태 변경 캡슐화
    // =============================================

    public void markAsSent() {
        if (this.messageStatus != MessageStatus.PENDING) {
            throw new IllegalStateException("메시지가 PENDING 상태여야 SENT 로 전환 가능합니다. 현재 상태: " + this.messageStatus);
        }
        this.messageStatus = MessageStatus.SENT;
        this.sentAt = Instant.now();
    }

    public void markAsDelivered() {
        if (this.messageStatus != MessageStatus.SENT) {
            throw new IllegalStateException("메시지가 SENT 상태여야 DELIVERED 로 전환 가능합니다. 현재 상태: " + this.messageStatus);
        }
        this.messageStatus = MessageStatus.DELIVERED;
        this.deliveredAt = Instant.now();
    }

    public void markAsRead() {
        if (this.messageStatus == MessageStatus.FAILED || this.messageStatus == MessageStatus.PENDING) {
            throw new IllegalStateException("PENDING 또는 FAILED 상태의 메시지는 READ 로 전환 불가합니다. 현재 상태: " + this.messageStatus);
        }
        this.messageStatus = MessageStatus.READ;
        this.readAt = Instant.now();
    }

    public void markAsFailed() {
        this.messageStatus = MessageStatus.FAILED;
    }

    /**
     * 메시지의 초기 미읽음 멤버 수 설정 (발송 시 호출).
     *
     * @param memberCount 채널 전체 멤버 수
     */
    public void initUnreadCount(int memberCount) {
        this.unreadCount = Math.max(0, memberCount - 1);
    }

    /**
     * 읽음 멤버 수 감소 (Kafka Consumer 호출).
     * MAX(0, unreadCount-1) 로 하한 보장.
     */
    public void decrementUnread() {
        this.unreadCount = Math.max(0, this.unreadCount - 1);
    }
}
