package com.example.chat.system.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 채널 엔티티
 * 메시지를 발행할 권한을 가진 주체 (예: 마케팅, 공지, 이벤트 등)
 */
@Entity
@Table(name = "channels")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Channel extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "channel_id")
    private Long id;

    @Column(name = "channel_name", nullable = false, length = 100)
    private String channelName;

    @Column(name = "channel_type", nullable = false, length = 50)
    private String channelType; // MARKETING, NOTICE, EVENT 등

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId; // 채널 담당자 ID

    /**
     * 채널 활성화
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * 채널 비활성화
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * 채널 정보 수정
     */
    public void updateInfo(String channelName, String description) {
        if (channelName != null && !channelName.isBlank()) {
            this.channelName = channelName;
        }
        if (description != null) {
            this.description = description;
        }
    }
}