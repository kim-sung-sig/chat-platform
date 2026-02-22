package com.example.chat.domain.friendship;

import java.time.Instant;

import com.example.chat.domain.service.DomainException;
import com.example.chat.domain.user.UserId;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 친구 관계 Aggregate Root
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public class Friendship {
    private FriendshipId id;
    private UserId userId; // 관계 요청자 (또는 소유자)
    private UserId friendId; // 친구 (대상자)
    private Instant createdAt;
    private FriendshipStatus status; // PENDING, ACCEPTED, BLOCKED
    private String nickname; // 친구 별칭 (선택)
    private boolean favorite; // 즐겨찾기 여부
    private Instant updatedAt;

    /**
     * 친구 요청 생성
     */
    public static Friendship requestFriendship(UserId userId, UserId friendId) {
        if (userId.equals(friendId)) {
            throw new DomainException("Cannot add yourself as a friend");
        }

        Instant now = Instant.now();
        return Friendship.builder()
                .id(FriendshipId.generate())
                .userId(userId)
                .friendId(friendId)
                .createdAt(now)
                .status(FriendshipStatus.PENDING)
                .favorite(false)
                .updatedAt(now)
                .build();
    }

    // === Business Methods ===

    /**
     * 친구 요청 수락
     */
    public void accept() {
        if (status != FriendshipStatus.PENDING) {
            throw new DomainException("Only pending requests can be accepted");
        }

        status = FriendshipStatus.ACCEPTED;
        updatedAt = Instant.now();
    }

    /**
     * 친구 차단
     */
    public void block() {
        status = FriendshipStatus.BLOCKED;
        updatedAt = Instant.now();
    }

    /**
     * 친구 차단 해제
     */
    public void unblock() {
        if (status != FriendshipStatus.BLOCKED) {
            throw new DomainException("Only blocked relationships can be unblocked");
        }

        status = FriendshipStatus.ACCEPTED;
        updatedAt = Instant.now();
    }

    /**
     * 친구 별칭 설정
     */
    public void updateNickname(String nickname) {
        if (!isAccepted()) {
            throw new DomainException("Can only set nickname for accepted friends");
        }

        this.nickname = nickname;
        updatedAt = Instant.now();
    }

    /**
     * 즐겨찾기 토글
     */
    public void toggleFavorite() {
        if (!isAccepted()) {
            throw new DomainException("Can only favorite accepted friends");
        }

        favorite = !favorite;
        updatedAt = Instant.now();
    }

    // === Query Methods ===

    public boolean isAccepted() {
        return status == FriendshipStatus.ACCEPTED;
    }

    public boolean isPending() {
        return status == FriendshipStatus.PENDING;
    }

    public boolean isBlocked() {
        return status == FriendshipStatus.BLOCKED;
    }

    public boolean isFavoriteFriend() {
        return favorite && isAccepted();
    }

    /**
     * Storage Layer에서 재구성
     */
    public static Friendship fromStorage(
            FriendshipId id,
            UserId userId,
            UserId friendId,
            Instant createdAt,
            FriendshipStatus status,
            String nickname,
            boolean favorite,
            Instant updatedAt) {
        return Friendship.builder()
                .id(id)
                .userId(userId)
                .friendId(friendId)
                .createdAt(createdAt)
                .status(status)
                .nickname(nickname)
                .favorite(favorite)
                .updatedAt(updatedAt)
                .build();
    }
}
