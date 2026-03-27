package com.example.chat.storage.domain.entity;

import com.example.chat.common.core.enums.FriendshipStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 친구 관계 JPA Entity.
 * createdAt / updatedAt 생명주기는 {@link BaseEntity} 에서 관리한다.
 */
@Entity
@Table(name = "chat_friendships", indexes = {
        @Index(name = "idx_friendship_user_id", columnList = "user_id"),
        @Index(name = "idx_friendship_friend_id", columnList = "friend_id"),
        @Index(name = "idx_user_status", columnList = "user_id, status")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_friendship", columnNames = { "user_id", "friend_id" })
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatFriendshipEntity extends BaseEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

    @Column(name = "friend_id", length = 36, nullable = false)
    private String friendId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private FriendshipStatus status;

    @Column(name = "nickname", length = 100)
    private String nickname;

    @Column(name = "favorite", nullable = false)
    private boolean favorite = false;

    private ChatFriendshipEntity(String id, String userId, String friendId, FriendshipStatus status) {
        this.id = id;
        this.userId = userId;
        this.friendId = friendId;
        this.status = status;
        this.favorite = false;
    }

    /**
     * 새 친구 관계 엔티티를 생성하는 팩토리 메서드.
     */
    public static ChatFriendshipEntity create(String id, String userId, String friendId,
                                               FriendshipStatus status) {
        return new ChatFriendshipEntity(id, userId, friendId, status);
    }

    // =============================================
    // 비즈니스 메서드 - 상태 변경 캡슐화
    // =============================================

    public void accept() {
        if (this.status != FriendshipStatus.PENDING) {
            throw new IllegalStateException("PENDING 상태의 요청만 수락할 수 있습니다. 현재 상태: " + this.status);
        }
        this.status = FriendshipStatus.ACCEPTED;
    }

    public void block() {
        if (this.status == FriendshipStatus.BLOCKED) {
            throw new IllegalStateException("이미 차단된 관계입니다.");
        }
        this.status = FriendshipStatus.BLOCKED;
    }

    public void unblock() {
        if (this.status != FriendshipStatus.BLOCKED) {
            throw new IllegalStateException("차단된 관계만 차단 해제할 수 있습니다. 현재 상태: " + this.status);
        }
        this.status = FriendshipStatus.ACCEPTED;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void toggleFavorite() {
        this.favorite = !this.favorite;
    }
}
