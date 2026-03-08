package com.example.chat.storage.entity;

import java.time.Instant;

import com.example.chat.common.core.enums.MessageStatus;
import com.example.chat.common.core.enums.MessageType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chat_messages", indexes = {
        @Index(name = "idx_chat_message_channel_created", columnList = "channel_id, created_at"),
        @Index(name = "idx_chat_message_sender", columnList = "sender_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
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
    @Builder.Default
    private MessageStatus messageStatus = MessageStatus.PENDING;

    @Column(name = "content_text", length = 5000)
    private String contentText;

    @Column(name = "content_media_url", length = 500)
    private String contentMediaUrl;

    @Column(name = "content_file_name", length = 255)
    private String contentFileName;

    @Column(name = "content_file_size")
    private Long contentFileSize;

    @Column(name = "content_mime_type", length = 100)
    private String contentMimeType;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Column(name = "read_at")
    private Instant readAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null || createdAt.equals(Instant.EPOCH)) {
            createdAt = Instant.now();
        }
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
}
