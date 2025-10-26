package com.example.chat.domain.entity;

import com.example.chat.common.dto.UserId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

/**
 * Domain entity representing a chat message. Contains behavioral methods for status transitions.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    private Long id;
    private String channelId;
    private UserId senderId;
    private String content;
    private String messageStatus; // SENT, DELIVERED, READ
    private OffsetDateTime createdAt;

    // Domain behavior: mark delivered
    public void markDelivered() {
        if (!"DELIVERED".equals(this.messageStatus)) {
            this.messageStatus = "DELIVERED";
        }
    }

    // Domain behavior: mark read
    public void markRead() {
        if (!"READ".equals(this.messageStatus)) {
            this.messageStatus = "READ";
        }
    }

    // Domain behavior: update content with business rules (e.g., length limit)
    public void updateContent(String newContent) {
        if (newContent == null) throw new IllegalArgumentException("content must not be null");
        if (newContent.length() > 4000) throw new IllegalArgumentException("content too long");
        this.content = newContent;
    }

    // Validate minimal invariants
    public void validateForSend() {
        if (this.channelId == null || this.channelId.isBlank()) throw new IllegalStateException("channelId required");
        if (this.senderId == null) throw new IllegalStateException("senderId required");
        if (this.content == null || this.content.isBlank()) throw new IllegalStateException("content required");
    }
}