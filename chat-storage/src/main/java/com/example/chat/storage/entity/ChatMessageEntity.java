package com.example.chat.storage.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "ms_chat_message", indexes = {
        @Index(name = "idx_chat_message_channel_created", columnList = "channel_id, created_at")
})
public class ChatMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ms_chat_message_id_gen")
    @SequenceGenerator(name = "ms_chat_message_id_gen", sequenceName = "ms_chat_message_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "channel_id", nullable = false, length = 128)
    private String channelId;

    // store sender id as numeric (bigint)
    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Column(name = "content", nullable = false, length = 4000)
    private String content;

    @Column(name = "message_status", nullable = false, length = 32)
    private String messageStatus;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) this.createdAt = OffsetDateTime.now();
        if (this.messageStatus == null) this.messageStatus = "SENT";
    }

}