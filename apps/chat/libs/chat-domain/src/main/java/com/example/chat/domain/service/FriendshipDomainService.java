package com.example.chat.domain.service;

import org.springframework.stereotype.Service;

import com.example.chat.domain.friendship.Friendship;
import com.example.chat.domain.user.User;

/**
 * 친구 관계 도메인 서비스
 *
 * DDD Domain Service의 역할:
 * - User + Friendship Aggregate 간 협력 조율
 * - 양방향 친구 관계 생성 규칙 캡슐화
 * - 복잡한 도메인 규칙 검증
 */
@Service
public class FriendshipDomainService {

    /**
     * 친구 요청 생성
     *
     * Domain Rule:
     * - 양방향 관계 생성 (A→B, B→A)
     * - 두 사용자 모두 활성 상태여야 함
     * - 자기 자신과는 친구 관계를 맺을 수 없음
     */
    public FriendshipPair requestFriendship(User requester, User target) {
        // Early Return: 자기 자신 체크
        if (requester.getId().equals(target.getId())) {
            throw new DomainException("Cannot add yourself as a friend");
        }

        // Early Return: 사용자 상태 체크
        if (!requester.canSendMessage()) {
            throw new DomainException("Requester is not in active status");
        }
        if (!target.canSendMessage()) {
            throw new DomainException("Target user is not in active status");
        }

        // 양방향 관계 생성
        Friendship requestToTarget = Friendship.requestFriendship(requester.getId(), target.getId());
        Friendship requestFromTarget = Friendship.requestFriendship(target.getId(), requester.getId());

        return new FriendshipPair(requestToTarget, requestFromTarget);
    }

    /**
     * 친구 요청 수락
     *
     * Domain Rule:
     * - 양방향 모두 ACCEPTED 상태로 변경
     * - PENDING 상태의 요청만 수락 가능
     */
    public void acceptFriendship(Friendship myRequest, Friendship theirRequest) {
        // Early Return: 상태 검증
        if (!myRequest.isPending()) {
            throw new DomainException("Can only accept pending requests");
        }
        if (!theirRequest.isPending()) {
            throw new DomainException("Mutual request is not pending");
        }

        // Early Return: 양방향 관계 검증
        if (!myRequest.getUserId().equals(theirRequest.getFriendId()) ||
                !myRequest.getFriendId().equals(theirRequest.getUserId())) {
            throw new DomainException("Invalid mutual friendship relationship");
        }

        // 양방향 수락
        myRequest.accept();
        theirRequest.accept();
    }

    /**
     * 친구 차단
     */
    public void blockFriend(Friendship friendship) {
        friendship.block();
    }

    /**
     * 친구 차단 해제
     */
    public void unblockFriend(Friendship friendship) {
        friendship.unblock();
    }

    /**
     * 양방향 친구 관계 Pair
     * (Record used as per request)
     */
    public record FriendshipPair(
            Friendship first, // 요청자 → 대상
            Friendship second // 대상 → 요청자
    ) {
        public Friendship getRequestToTarget() {
            return first;
        }

        public Friendship getRequestFromTarget() {
            return second;
        }
    }
}
