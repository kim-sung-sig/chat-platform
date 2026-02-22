package com.example.chat.storage.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 채널 멤버 JPA Entity
 */
@Entity
@Table(name = "chat_channel_members", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "channel_id", "user_id" })
}, indexes = {
        @Index(name = "idx_channel_member_channel", columnList = "channel_id"),
        @Index(name = "idx_channel_member_user", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ChatChannelMemberEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "channel_id", nullable = false, length = 36)
    private String channelId;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Column(name = "joined_at", nullable = false)
    @Builder.Default
    private Instant joinedAt = Instant.now();

    @PrePersist
    public void prePersist() {
        if (joinedAt == null || joinedAt.equals(Instant.EPOCH)) {
            joinedAt = Instant.now();
        }
    }
}
