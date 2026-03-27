package com.example.chat.storage.domain.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 채널 멤버 JPA Entity.
 * joinedAt 을 createdAt 대신 사용하며, Long PK 전략을 유지한다.
 * BaseEntity 를 상속하지 않는다 (joinedAt / Long PK 로 인한 의도적 예외).
 */
@Entity
@Table(name = "chat_channel_members", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "channel_id", "user_id" })
}, indexes = {
        @Index(name = "idx_channel_member_channel", columnList = "channel_id"),
        @Index(name = "idx_channel_member_user", columnList = "user_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatChannelMemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "channel_id", nullable = false, length = 36)
    private String channelId;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt;

    private ChatChannelMemberEntity(String channelId, String userId) {
        this.channelId = channelId;
        this.userId = userId;
        this.joinedAt = Instant.now();
    }

    /**
     * 새 채널 멤버 엔티티를 생성하는 팩토리 메서드.
     */
    public static ChatChannelMemberEntity create(String channelId, String userId) {
        return new ChatChannelMemberEntity(channelId, userId);
    }
}
