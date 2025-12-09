package com.example.chat.storage.entity;

import com.example.chat.domain.message.MessageStatus;
import com.example.chat.domain.message.MessageType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "chat_messages", indexes = {
        @Index(name = "idx_chat_message_channel_created", columnList = "channel_id, created_at"),
        @Index(name = "idx_chat_message_sender", columnList = "sender_id")
})
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
    private MessageStatus messageStatus;

    // MessageContent 필드들
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

    // 타임스탬프
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Column(name = "read_at")
    private Instant readAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        if (this.messageStatus == null) {
            this.messageStatus = MessageStatus.PENDING;
        }
    }

}
