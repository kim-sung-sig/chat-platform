package com.example.chat.domain.channel;

import com.example.chat.domain.user.UserId;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * 채널 Aggregate Root
 */
@Getter
@Builder
public class Channel {
    private final ChannelId id;
    private String name;
    private String description;
    private final ChannelType type;
    private final UserId ownerId;
    private final Set<UserId> memberIds;
    private boolean active;
    private final Instant createdAt;
    private Instant updatedAt;

    /**
     * 새로운 채널 생성
     */
    public static Channel create(String name, ChannelType type, UserId ownerId) {
        Set<UserId> memberIds = new HashSet<>();
        memberIds.add(ownerId);  // 생성자는 자동으로 멤버에 포함

        return Channel.builder()
                .id(ChannelId.generate())
                .name(name)
                .type(type)
                .ownerId(ownerId)
                .memberIds(memberIds)
                .active(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    /**
     * 멤버 추가
     */
    public void addMember(UserId userId) {
        if (!this.active) {
            throw new IllegalStateException("Cannot add member to inactive channel");
        }
        if (this.memberIds.contains(userId)) {
            throw new IllegalArgumentException("User is already a member");
        }

        this.memberIds.add(userId);
        this.updatedAt = Instant.now();
    }

    /**
     * 멤버 제거
     */
    public void removeMember(UserId userId) {
        if (userId.equals(this.ownerId)) {
            throw new IllegalArgumentException("Cannot remove channel owner");
        }
        if (!this.memberIds.contains(userId)) {
            throw new IllegalArgumentException("User is not a member");
        }

        this.memberIds.remove(userId);
        this.updatedAt = Instant.now();
    }

    /**
     * 멤버인지 확인
     */
    public boolean isMember(UserId userId) {
        return this.memberIds.contains(userId);
    }

    /**
     * 소유자인지 확인
     */
    public boolean isOwner(UserId userId) {
        return this.ownerId.equals(userId);
    }

    /**
     * 채널 비활성화
     */
    public void deactivate() {
        this.active = false;
        this.updatedAt = Instant.now();
    }

    /**
     * 채널 활성화
     */
    public void activate() {
        this.active = true;
        this.updatedAt = Instant.now();
    }

    /**
     * 채널 정보 수정
     */
    public void updateInfo(String name, String description) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        this.description = description;
        this.updatedAt = Instant.now();
    }

    /**
     * 멤버 수 조회
     */
    public int getMemberCount() {
        return this.memberIds.size();
    }
}
