package com.example.chat.storage.domain.entity;

import com.example.chat.common.core.enums.ChannelType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 채널 JPA Entity.
 * createdAt / updatedAt 생명주기는 {@link BaseEntity} 에서 관리한다.
 */
@Entity
@Table(name = "chat_channels", indexes = {
        @Index(name = "idx_chat_channel_owner", columnList = "owner_id"),
        @Index(name = "idx_chat_channel_type", columnList = "channel_type")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatChannelEntity extends BaseEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel_type", nullable = false, length = 20)
    private ChannelType channelType;

    @Column(name = "owner_id", nullable = false, length = 36)
    private String ownerId;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    private ChatChannelEntity(String id, String name, String description,
                               ChannelType channelType, String ownerId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.channelType = channelType;
        this.ownerId = ownerId;
        this.active = true;
    }

    /**
     * 새 채널 엔티티를 생성하는 팩토리 메서드.
     */
    public static ChatChannelEntity create(String id, String name, String description,
                                           ChannelType channelType, String ownerId) {
        return new ChatChannelEntity(id, name, description, channelType, ownerId);
    }

    // =============================================
    // 비즈니스 메서드 - 상태 변경 캡슐화
    // =============================================

    public void deactivate() {
        if (!this.active) {
            throw new IllegalStateException("이미 비활성화된 채널입니다: " + this.id);
        }
        this.active = false;
    }

    public void activate() {
        if (this.active) {
            throw new IllegalStateException("이미 활성화된 채널입니다: " + this.id);
        }
        this.active = true;
    }

    public void updateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("채널 이름은 비어 있을 수 없습니다.");
        }
        this.name = name;
    }

    public void updateDescription(String description) {
        this.description = description;
    }
}
