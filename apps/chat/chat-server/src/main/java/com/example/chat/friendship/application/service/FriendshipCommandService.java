package com.example.chat.friendship.application.service;

import com.example.chat.common.event.FriendAcceptedEvent;
import com.example.chat.common.event.FriendBlockedEvent;
import com.example.chat.common.event.FriendRequestedEvent;
import com.example.chat.common.core.exception.ChatErrorCode;
import com.example.chat.common.core.enums.FriendshipStatus;
import com.example.chat.shared.exception.ChatException;
import com.example.chat.storage.domain.entity.ChatFriendshipEntity;
import com.example.chat.storage.domain.entity.UserEntity;
import com.example.chat.storage.domain.repository.JpaFriendshipRepository;
import com.example.chat.storage.domain.repository.JpaUserRepository;
import com.example.chat.friendship.rest.dto.response.FriendshipResponse;
import com.example.chat.shared.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * 친구 관계 Command Service (Phase 2: JPA Entity 직접 사용)
 *
 * 책임: 친구 요청/수락/거절/차단/삭제 Use Case
 * Entity 비즈니스 메서드로 상태 변경 캡슐화
 */
@Service
@Transactional
public class FriendshipCommandService {

    private static final Logger logger = LoggerFactory.getLogger(FriendshipCommandService.class);

    private final JpaFriendshipRepository friendshipRepository;
    private final JpaUserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    public FriendshipCommandService(
            JpaFriendshipRepository friendshipRepository,
            JpaUserRepository userRepository,
            ApplicationEventPublisher eventPublisher) {
        this.friendshipRepository = friendshipRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    /** 친구 요청 */
    public FriendshipResponse requestFriendship(String requesterId, String targetId) {
        logger.info("Requesting friendship: {} → {}", requesterId, targetId);

        verifyUserExists(requesterId);
        verifyUserExists(targetId);

        // 기존 관계 확인
        friendshipRepository.findByUserIdAndFriendId(requesterId, targetId).ifPresent(existing -> {
            if (existing.getStatus() == FriendshipStatus.BLOCKED)  throw new ChatException(ChatErrorCode.FRIENDSHIP_BLOCKED);
            if (existing.getStatus() == FriendshipStatus.PENDING)  throw new ChatException(ChatErrorCode.FRIENDSHIP_ALREADY_EXISTS);
            if (existing.getStatus() == FriendshipStatus.ACCEPTED) throw new ChatException(ChatErrorCode.FRIENDSHIP_ALREADY_FRIENDS);
        });
        friendshipRepository.findByUserIdAndFriendId(targetId, requesterId).ifPresent(existing -> {
            if (existing.getStatus() == FriendshipStatus.BLOCKED) throw new ChatException(ChatErrorCode.FRIENDSHIP_BLOCKED);
        });

        // 양방향 생성
        ChatFriendshipEntity toTarget = buildFriendship(requesterId, targetId);
        ChatFriendshipEntity fromTarget = buildFriendship(targetId, requesterId);
        ChatFriendshipEntity saved = friendshipRepository.save(toTarget);
        friendshipRepository.save(fromTarget);

        eventPublisher.publishEvent(new FriendRequestedEvent(requesterId, targetId, Instant.now()));
        logger.info("Friend request created: {}", saved.getId());
        return FriendshipResponse.fromEntity(saved);
    }

    /** 친구 요청 수락 */
    public FriendshipResponse acceptFriendRequest(String userId, String requestId) {
        logger.info("Accepting friend request: userId={}, requestId={}", userId, requestId);

        ChatFriendshipEntity myRequest = findFriendshipById(requestId);
        if (!myRequest.getFriendId().equals(userId)) {
            throw new ChatException(ChatErrorCode.DOMAIN_RULE_VIOLATION);
        }

        ChatFriendshipEntity theirRequest = friendshipRepository
                .findByUserIdAndFriendId(userId, myRequest.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(ChatErrorCode.FRIENDSHIP_NOT_FOUND));

        myRequest.accept();
        theirRequest.accept();

        friendshipRepository.save(myRequest);
        ChatFriendshipEntity saved = friendshipRepository.save(theirRequest);

        eventPublisher.publishEvent(new FriendAcceptedEvent(userId, myRequest.getUserId(), Instant.now()));
        logger.info("Friend request accepted: {}", requestId);
        return FriendshipResponse.fromEntity(saved);
    }

    /** 친구 요청 거절 */
    public void rejectFriendRequest(String userId, String requestId) {
        logger.info("Rejecting friend request: userId={}, requestId={}", userId, requestId);

        ChatFriendshipEntity myRequest = findFriendshipById(requestId);
        if (!myRequest.getFriendId().equals(userId)) {
            throw new ChatException(ChatErrorCode.DOMAIN_RULE_VIOLATION);
        }
        if (myRequest.getStatus() != FriendshipStatus.PENDING) {
            throw new ChatException(ChatErrorCode.DOMAIN_RULE_VIOLATION);
        }

        friendshipRepository.deleteById(requestId);
        friendshipRepository.findByUserIdAndFriendId(userId, myRequest.getUserId())
                .ifPresent(it -> friendshipRepository.deleteById(it.getId()));

        logger.info("Friend request rejected: {}", requestId);
    }

    /** 친구 차단 */
    public FriendshipResponse blockFriend(String userId, String friendId) {
        logger.info("Blocking friend: {} → {}", userId, friendId);

        ChatFriendshipEntity friendship = findFriendshipByUserAndFriend(userId, friendId);
        friendship.block();
        ChatFriendshipEntity saved = friendshipRepository.save(friendship);

        eventPublisher.publishEvent(new FriendBlockedEvent(userId, friendId, Instant.now()));
        return FriendshipResponse.fromEntity(saved);
    }

    /** 친구 차단 해제 */
    public FriendshipResponse unblockFriend(String userId, String friendId) {
        logger.info("Unblocking friend: {} → {}", userId, friendId);

        ChatFriendshipEntity friendship = findFriendshipByUserAndFriend(userId, friendId);
        friendship.unblock();
        return FriendshipResponse.fromEntity(friendshipRepository.save(friendship));
    }

    /** 친구 삭제 */
    public void deleteFriend(String userId, String friendId) {
        logger.info("Deleting friend: {} ↔ {}", userId, friendId);

        ChatFriendshipEntity friendship = findFriendshipByUserAndFriend(userId, friendId);
        friendshipRepository.deleteById(friendship.getId());
        friendshipRepository.findByUserIdAndFriendId(friendId, userId)
                .ifPresent(it -> friendshipRepository.deleteById(it.getId()));
    }

    /** 친구 별칭 설정 */
    public FriendshipResponse setFriendNickname(String userId, String friendId, String nickname) {
        ChatFriendshipEntity friendship = findFriendshipByUserAndFriend(userId, friendId);
        friendship.updateNickname(nickname);
        return FriendshipResponse.fromEntity(friendshipRepository.save(friendship));
    }

    /** 즐겨찾기 토글 */
    public FriendshipResponse toggleFavorite(String userId, String friendId) {
        ChatFriendshipEntity friendship = findFriendshipByUserAndFriend(userId, friendId);
        friendship.toggleFavorite();
        return FriendshipResponse.fromEntity(friendshipRepository.save(friendship));
    }

    // =============================================
    // Private Helpers
    // =============================================

    /** 사용자 존재 확인 (없으면 예외) */
    private void verifyUserExists(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException(ChatErrorCode.USER_NOT_FOUND);
        }
    }

    private ChatFriendshipEntity findFriendshipById(String id) {
        return friendshipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ChatErrorCode.FRIENDSHIP_NOT_FOUND));
    }

    private ChatFriendshipEntity findFriendshipByUserAndFriend(String userId, String friendId) {
        return friendshipRepository.findByUserIdAndFriendId(userId, friendId)
                .orElseThrow(() -> new ResourceNotFoundException(ChatErrorCode.FRIENDSHIP_NOT_FOUND));
    }

    private ChatFriendshipEntity buildFriendship(String userId, String friendId) {
        return ChatFriendshipEntity.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .friendId(friendId)
                .status(FriendshipStatus.PENDING)
                .build();
    }
}
