package com.example.chat.domain.channel;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.example.chat.domain.user.UserId;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 채널 Aggregate Root
 *
 * 책임:
 * - 채널 생명주기 관리
 * - 멤버 관리
 * - 채널 정보 유지
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public class Channel {
    private ChannelId id;
    private String name;
    private String description;
    private ChannelType type;
    private UserId ownerId;
    private Set<UserId> memberIds;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    /**
     * 멤버 추가
     */
    public void addMember(UserId userId) {
        if (!active) {
            throw new IllegalStateException("Cannot add member to inactive channel");
        }
        if (memberIds.contains(userId)) {
            throw new IllegalArgumentException("User is already a member");
        }

        memberIds.add(userId);
        updatedAt = Instant.now();
    }

    /**
     * 멤버 제거
     */
    public void removeMember(UserId userId) {
        if (userId.equals(ownerId)) {
            throw new IllegalArgumentException("Cannot remove channel owner");
        }
        if (!memberIds.contains(userId)) {
            throw new IllegalArgumentException("User is not a member");
        }

        memberIds.remove(userId);
        updatedAt = Instant.now();
    }

    /**
     * 멤버인지 확인
     */
    public boolean isMember(UserId userId) {
        return memberIds.contains(userId);
    }

    /**
     * 소유자인지 확인
     */
    public boolean isOwner(UserId userId) {
        return ownerId.equals(userId);
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
        return memberIds.size();
    }

    /**
     * 내부 컬렉션 노출 지양 - 불변 셋 반환
     */
    public Set<UserId> getMemberIds() {
        return Collections.unmodifiableSet(memberIds);
    }

    /**
     * 새로운 채널 생성
     */
    public static Channel create(String name, ChannelType type, UserId ownerId) {
        Set<UserId> initialMembers = new HashSet<>();
        initialMembers.add(ownerId);
        Instant now = Instant.now();

        return Channel.builder()
                .id(ChannelId.generate())
                .name(name)
                .type(type)
                .ownerId(ownerId)
                .memberIds(initialMembers)
                .active(true)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    /**
     * Storage Layer에서 재구성
     */
    public static Channel fromStorage(
            ChannelId id,
            String name,
            String description,
            ChannelType type,
            UserId ownerId,
            Set<UserId> memberIds,
            boolean active,
            Instant createdAt,
            Instant updatedAt) {
        return Channel.builder()
                .id(id)
                .name(name)
                .description(description)
                .type(type)
                .ownerId(ownerId)
                .memberIds(new HashSet<>(memberIds))
                .active(active)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}
