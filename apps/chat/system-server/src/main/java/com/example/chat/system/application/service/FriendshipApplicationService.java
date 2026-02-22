package com.example.chat.system.application.service;

import com.example.chat.common.event.FriendAcceptedEvent;
import com.example.chat.common.event.FriendBlockedEvent;
import com.example.chat.common.event.FriendRequestedEvent;
import com.example.chat.domain.friendship.Friendship;
import com.example.chat.domain.friendship.FriendshipId;
import com.example.chat.domain.friendship.FriendshipRepository;
import com.example.chat.domain.service.DomainException;
import com.example.chat.domain.service.FriendshipDomainService;
import com.example.chat.domain.user.User;
import com.example.chat.domain.user.UserId;
import com.example.chat.domain.user.UserRepository;
import com.example.chat.system.application.dto.response.FriendshipResponse;
import com.example.chat.system.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 친구 관리 Application Service
 *
 * 책임:
 * - Use Case 오케스트레이션
 * - Domain Service + Repository 협력 조율
 * - 이벤트 발행
 * - DTO 변환
 */
@Service
@Transactional
public class FriendshipApplicationService {
    private static final Logger logger = LoggerFactory.getLogger(FriendshipApplicationService.class);

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final FriendshipDomainService friendshipDomainService;
    private final ApplicationEventPublisher eventPublisher;

    public FriendshipApplicationService(
            FriendshipRepository friendshipRepository,
            UserRepository userRepository,
            FriendshipDomainService friendshipDomainService,
            ApplicationEventPublisher eventPublisher) {
        this.friendshipRepository = friendshipRepository;
        this.userRepository = userRepository;
        this.friendshipDomainService = friendshipDomainService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * 친구 요청
     */
    public FriendshipResponse requestFriendship(String requesterId, String targetId) {
        logger.info("Requesting friendship: {} → {}", requesterId, targetId);

        // 1. User Aggregate 조회
        User requester = findUserById(requesterId);
        User target = findUserById(targetId);

        // 2. 기존 관계 확인
        UserId requesterUserId = UserId.of(requesterId);
        UserId targetUserId = UserId.of(targetId);

        friendshipRepository.findByUserIdAndFriendId(requesterUserId, targetUserId).ifPresent(existing -> {
            if (existing.isBlocked())
                throw new DomainException("Cannot send request to blocked user");
            if (existing.isPending())
                throw new DomainException("Friend request already sent");
            if (existing.isAccepted())
                throw new DomainException("Already friends");
        });

        // 3. 상대방이 나를 차단했는지 확인
        friendshipRepository.findByUserIdAndFriendId(targetUserId, requesterUserId).ifPresent(existing -> {
            if (existing.isBlocked()) {
                throw new DomainException("Cannot send request - you are blocked");
            }
        });

        // 4. Domain Service를 통한 친구 요청 생성 (양방향)
        var result = friendshipDomainService.requestFriendship(requester, target);
        Friendship requestToTarget = result.requestToTarget();
        Friendship requestFromTarget = result.requestFromTarget();

        // 5. 저장
        Friendship saved = friendshipRepository.save(requestToTarget);
        friendshipRepository.save(requestFromTarget);

        // 6. 이벤트 발행
        eventPublisher.publishEvent(
                new FriendRequestedEvent(requesterId, targetId, Instant.now()));

        logger.info("Friend request created: {}", saved.getId().value());
        return FriendshipResponse.from(saved);
    }

    /**
     * 친구 요청 수락
     */
    public FriendshipResponse acceptFriendRequest(String userId, String requestId) {
        logger.info("Accepting friend request: userId={}, requestId={}", userId, requestId);

        // 1. 내 요청 조회 (상대방 → 나)
        Friendship myRequest = friendshipRepository.findById(FriendshipId.of(requestId))
                .orElseThrow(() -> new ResourceNotFoundException("Friend request not found"));

        // 2. 권한 확인 (내가 friendId여야 함)
        if (!myRequest.getFriendId().value().equals(userId)) {
            throw new DomainException("Not authorized to accept this request");
        }

        // 3. 양방향 관계 조회 (나 → 상대방)
        Friendship theirRequest = friendshipRepository.findByUserIdAndFriendId(
                UserId.of(userId),
                myRequest.getUserId()).orElseThrow(() -> new ResourceNotFoundException("Mutual request not found"));

        // 4. Domain Service를 통한 수락
        friendshipDomainService.acceptFriendship(theirRequest, myRequest);

        // 5. 저장
        friendshipRepository.save(myRequest);
        Friendship saved = friendshipRepository.save(theirRequest);

        // 6. 이벤트 발행
        eventPublisher.publishEvent(
                new FriendAcceptedEvent(userId, myRequest.getUserId().value(), Instant.now()));

        logger.info("Friend request accepted: {}", requestId);
        return FriendshipResponse.from(saved);
    }

    /**
     * 친구 요청 거절 (삭제)
     */
    public void rejectFriendRequest(String userId, String requestId) {
        logger.info("Rejecting friend request: userId={}, requestId={}", userId, requestId);

        // 1. 요청 조회
        Friendship myRequest = friendshipRepository.findById(FriendshipId.of(requestId))
                .orElseThrow(() -> new ResourceNotFoundException("Friend request not found"));

        // 2. 권한 확인
        if (!myRequest.getFriendId().value().equals(userId)) {
            throw new DomainException("Not authorized to reject this request");
        }

        // 3. 상태 확인
        if (!myRequest.isPending()) {
            throw new DomainException("Only pending requests can be rejected");
        }

        // 4. 양방향 삭제
        friendshipRepository.deleteById(myRequest.getId());

        friendshipRepository.findByUserIdAndFriendId(UserId.of(userId), myRequest.getUserId())
                .ifPresent(it -> friendshipRepository.deleteById(it.getId()));

        logger.info("Friend request rejected: {}", requestId);
    }

    /**
     * 친구 목록 조회 (수락된 친구만)
     */
    @Transactional(readOnly = true)
    public List<FriendshipResponse> getFriendList(String userId) {
        logger.debug("Getting friend list for user: {}", userId);

        return friendshipRepository.findAcceptedFriendsByUserId(UserId.of(userId)).stream()
                .map(FriendshipResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 받은 친구 요청 목록 조회
     */
    @Transactional(readOnly = true)
    public List<FriendshipResponse> getPendingRequests(String userId) {
        logger.debug("Getting pending requests for user: {}", userId);

        return friendshipRepository.findPendingRequestsByFriendId(UserId.of(userId)).stream()
                .map(FriendshipResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 보낸 친구 요청 목록 조회
     */
    @Transactional(readOnly = true)
    public List<FriendshipResponse> getSentRequests(String userId) {
        logger.debug("Getting sent requests for user: {}", userId);

        return friendshipRepository.findPendingRequestsByUserId(UserId.of(userId)).stream()
                .map(FriendshipResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 친구 차단
     */
    public FriendshipResponse blockFriend(String userId, String friendId) {
        logger.info("Blocking friend: {} → {}", userId, friendId);

        Friendship friendship = friendshipRepository.findByUserIdAndFriendId(
                UserId.of(userId),
                UserId.of(friendId)).orElseThrow(() -> new ResourceNotFoundException("Friendship not found"));

        friendshipDomainService.blockFriend(friendship);
        Friendship saved = friendshipRepository.save(friendship);

        // 이벤트 발행
        eventPublisher.publishEvent(
                new FriendBlockedEvent(userId, friendId, Instant.now()));

        logger.info("Friend blocked: {}", friendship.getId().value());
        return FriendshipResponse.from(saved);
    }

    /**
     * 친구 차단 해제
     */
    public FriendshipResponse unblockFriend(String userId, String friendId) {
        logger.info("Unblocking friend: {} → {}", userId, friendId);

        Friendship friendship = friendshipRepository.findByUserIdAndFriendId(
                UserId.of(userId),
                UserId.of(friendId)).orElseThrow(() -> new ResourceNotFoundException("Friendship not found"));

        friendshipDomainService.unblockFriend(friendship);
        Friendship saved = friendshipRepository.save(friendship);

        logger.info("Friend unblocked: {}", friendship.getId().value());
        return FriendshipResponse.from(saved);
    }

    /**
     * 친구 삭제
     */
    public void deleteFriend(String userId, String friendId) {
        logger.info("Deleting friend: {} ↔ {}", userId, friendId);

        Friendship friendship = friendshipRepository.findByUserIdAndFriendId(
                UserId.of(userId),
                UserId.of(friendId)).orElseThrow(() -> new ResourceNotFoundException("Friendship not found"));

        // 양방향 삭제
        friendshipRepository.deleteById(friendship.getId());

        friendshipRepository.findByUserIdAndFriendId(UserId.of(friendId), UserId.of(userId))
                .ifPresent(it -> friendshipRepository.deleteById(it.getId()));

        logger.info("Friend deleted successfully");
    }

    /**
     * 친구 별칭 설정
     */
    public FriendshipResponse setFriendNickname(String userId, String friendId, String nickname) {
        logger.info("Setting nickname: {} → {} = {}", userId, friendId, nickname);

        Friendship friendship = friendshipRepository.findByUserIdAndFriendId(
                UserId.of(userId),
                UserId.of(friendId)).orElseThrow(() -> new ResourceNotFoundException("Friendship not found"));

        friendship.updateNickname(nickname);
        Friendship saved = friendshipRepository.save(friendship);

        logger.info("Nickname set successfully");
        return FriendshipResponse.from(saved);
    }

    /**
     * 즐겨찾기 토글
     */
    public FriendshipResponse toggleFavorite(String userId, String friendId) {
        logger.info("Toggling favorite: {} → {}", userId, friendId);

        Friendship friendship = friendshipRepository.findByUserIdAndFriendId(
                UserId.of(userId),
                UserId.of(friendId)).orElseThrow(() -> new ResourceNotFoundException("Friendship not found"));

        friendship.toggleFavorite();
        Friendship saved = friendshipRepository.save(friendship);

        logger.info("Favorite toggled: favorite={}", saved.isFavorite());
        return FriendshipResponse.from(saved);
    }

    /**
     * 즐겨찾기 친구 목록 조회
     */
    @Transactional(readOnly = true)
    public List<FriendshipResponse> getFavoriteFriends(String userId) {
        logger.debug("Getting favorite friends for user: {}", userId);

        return friendshipRepository.findFavoritesByUserId(UserId.of(userId)).stream()
                .map(FriendshipResponse::from)
                .collect(Collectors.toList());
    }

    // === Private Mapper Methods ===

    private User findUserById(String userId) {
        return userRepository.findById(UserId.of(userId))
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }
}
