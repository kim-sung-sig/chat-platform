package com.example.chat.system.application.service

import com.example.chat.common.event.FriendAcceptedEvent
import com.example.chat.common.event.FriendBlockedEvent
import com.example.chat.common.event.FriendRequestedEvent
import com.example.chat.domain.friendship.Friendship
import com.example.chat.domain.friendship.FriendshipId
import com.example.chat.domain.friendship.FriendshipRepository
import com.example.chat.domain.service.DomainException
import com.example.chat.domain.service.FriendshipDomainService
import com.example.chat.domain.user.User
import com.example.chat.domain.user.UserId
import com.example.chat.domain.user.UserRepository
import com.example.chat.system.application.dto.response.FriendshipResponse
import com.example.chat.system.application.dto.response.toResponse
import com.example.chat.system.exception.ResourceNotFoundException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

private val logger = KotlinLogging.logger {}

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
class FriendshipApplicationService(
	private val friendshipRepository: FriendshipRepository,
	private val userRepository: UserRepository,
	private val friendshipDomainService: FriendshipDomainService,
	private val eventPublisher: ApplicationEventPublisher
) {

	/**
	 * 친구 요청
	 */
	fun requestFriendship(requesterId: String, targetId: String): FriendshipResponse {
		logger.info { "Requesting friendship: $requesterId → $targetId" }

		// 1. User Aggregate 조회
		val requester = findUserById(requesterId)
		val target = findUserById(targetId)

		// 2. 기존 관계 확인
		val requesterUserId = UserId.of(requesterId)
		val targetUserId = UserId.of(targetId)

		friendshipRepository.findByUserIdAndFriendId(requesterUserId, targetUserId)?.let { existing ->
			when {
				existing.isBlocked() -> throw DomainException("Cannot send request to blocked user")
				existing.isPending() -> throw DomainException("Friend request already sent")
				existing.isAccepted() -> throw DomainException("Already friends")
				else -> Unit
			}
		}

		// 3. 상대방이 나를 차단했는지 확인
		friendshipRepository.findByUserIdAndFriendId(targetUserId, requesterUserId)?.let { existing ->
			if (existing.isBlocked()) {
				throw DomainException("Cannot send request - you are blocked")
			}
		}

		// 4. Domain Service를 통한 친구 요청 생성 (양방향)
		val (requestToTarget, requestFromTarget) = friendshipDomainService.requestFriendship(requester, target)

		// 5. 저장
		val saved = friendshipRepository.save(requestToTarget)
		friendshipRepository.save(requestFromTarget)

		// 6. 이벤트 발행
		eventPublisher.publishEvent(
			FriendRequestedEvent(requesterId, targetId, Instant.now())
		)

		logger.info { "Friend request created: ${saved.id.value}" }
		return saved.toResponse()
	}

	/**
	 * 친구 요청 수락
	 */
	fun acceptFriendRequest(userId: String, requestId: String): FriendshipResponse {
		logger.info { "Accepting friend request: userId=$userId, requestId=$requestId" }

		// 1. 내 요청 조회 (상대방 → 나)
		val myRequest = friendshipRepository.findById(FriendshipId.of(requestId))
			?: throw ResourceNotFoundException("Friend request not found")

		// 2. 권한 확인 (내가 friendId여야 함)
		require(myRequest.friendId.value == userId) {
			throw DomainException("Not authorized to accept this request")
		}

		// 3. 양방향 관계 조회 (나 → 상대방)
		val theirRequest = friendshipRepository.findByUserIdAndFriendId(
			UserId.of(userId),
			myRequest.userId
		) ?: throw ResourceNotFoundException("Mutual request not found")

		// 4. Domain Service를 통한 수락
		friendshipDomainService.acceptFriendship(theirRequest, myRequest)

		// 5. 저장
		friendshipRepository.save(myRequest)
		val saved = friendshipRepository.save(theirRequest)

		// 6. 이벤트 발행
		eventPublisher.publishEvent(
			FriendAcceptedEvent(userId, myRequest.userId.value, Instant.now())
		)

		logger.info { "Friend request accepted: $requestId" }
		return saved.toResponse()
	}

	/**
	 * 친구 요청 거절 (삭제)
	 */
	fun rejectFriendRequest(userId: String, requestId: String) {
		logger.info { "Rejecting friend request: userId=$userId, requestId=$requestId" }

		// 1. 요청 조회
		val myRequest = friendshipRepository.findById(FriendshipId.of(requestId))
			?: throw ResourceNotFoundException("Friend request not found")

		// 2. 권한 확인
		require(myRequest.friendId.value == userId) {
			throw DomainException("Not authorized to reject this request")
		}

		// 3. 상태 확인
		require(myRequest.isPending()) {
			throw DomainException("Only pending requests can be rejected")
		}

		// 4. 양방향 삭제
		friendshipRepository.deleteById(myRequest.id)

		friendshipRepository.findByUserIdAndFriendId(UserId.of(userId), myRequest.userId)
			?.let { friendshipRepository.deleteById(it.id) }

		logger.info { "Friend request rejected: $requestId" }
	}

	/**
	 * 친구 목록 조회 (수락된 친구만)
	 */
	@Transactional(readOnly = true)
	fun getFriendList(userId: String): List<FriendshipResponse> {
		logger.debug { "Getting friend list for user: $userId" }

		return friendshipRepository.findAcceptedFriendsByUserId(UserId.of(userId))
			.map { it.toResponse() }
	}

	/**
	 * 받은 친구 요청 목록 조회
	 */
	@Transactional(readOnly = true)
	fun getPendingRequests(userId: String): List<FriendshipResponse> {
		logger.debug { "Getting pending requests for user: $userId" }

		return friendshipRepository.findPendingRequestsByFriendId(UserId.of(userId))
			.map { it.toResponse() }
	}

	/**
	 * 보낸 친구 요청 목록 조회
	 */
	@Transactional(readOnly = true)
	fun getSentRequests(userId: String): List<FriendshipResponse> {
		logger.debug { "Getting sent requests for user: $userId" }

		return friendshipRepository.findPendingRequestsByUserId(UserId.of(userId))
			.map { it.toResponse() }
	}

	/**
	 * 친구 차단
	 */
	fun blockFriend(userId: String, friendId: String): FriendshipResponse {
		logger.info { "Blocking friend: $userId → $friendId" }

		val friendship = friendshipRepository.findByUserIdAndFriendId(
			UserId.of(userId),
			UserId.of(friendId)
		) ?: throw ResourceNotFoundException("Friendship not found")

		friendshipDomainService.blockFriend(friendship)
		val saved = friendshipRepository.save(friendship)

		// 이벤트 발행
		eventPublisher.publishEvent(
			FriendBlockedEvent(userId, friendId, Instant.now())
		)

		logger.info { "Friend blocked: ${friendship.id.value}" }
		return saved.toResponse()
	}

	/**
	 * 친구 차단 해제
	 */
	fun unblockFriend(userId: String, friendId: String): FriendshipResponse {
		logger.info { "Unblocking friend: $userId → $friendId" }

		val friendship = friendshipRepository.findByUserIdAndFriendId(
			UserId.of(userId),
			UserId.of(friendId)
		) ?: throw ResourceNotFoundException("Friendship not found")

		friendshipDomainService.unblockFriend(friendship)
		val saved = friendshipRepository.save(friendship)

		logger.info { "Friend unblocked: ${friendship.id.value}" }
		return saved.toResponse()
	}

	/**
	 * 친구 삭제
	 */
	fun deleteFriend(userId: String, friendId: String) {
		logger.info { "Deleting friend: $userId ↔ $friendId" }

		val friendship = friendshipRepository.findByUserIdAndFriendId(
			UserId.of(userId),
			UserId.of(friendId)
		) ?: throw ResourceNotFoundException("Friendship not found")

		// 양방향 삭제
		friendshipRepository.deleteById(friendship.id)

		friendshipRepository.findByUserIdAndFriendId(UserId.of(friendId), UserId.of(userId))
			?.let { friendshipRepository.deleteById(it.id) }

		logger.info { "Friend deleted successfully" }
	}

	/**
	 * 친구 별칭 설정
	 */
	fun setFriendNickname(userId: String, friendId: String, nickname: String): FriendshipResponse {
		logger.info { "Setting nickname: $userId → $friendId = $nickname" }

		val friendship = friendshipRepository.findByUserIdAndFriendId(
			UserId.of(userId),
			UserId.of(friendId)
		) ?: throw ResourceNotFoundException("Friendship not found")

		friendship.updateNickname(nickname)
		val saved = friendshipRepository.save(friendship)

		logger.info { "Nickname set successfully" }
		return saved.toResponse()
	}

	/**
	 * 즐겨찾기 토글
	 */
	fun toggleFavorite(userId: String, friendId: String): FriendshipResponse {
		logger.info { "Toggling favorite: $userId → $friendId" }

		val friendship = friendshipRepository.findByUserIdAndFriendId(
			UserId.of(userId),
			UserId.of(friendId)
		) ?: throw ResourceNotFoundException("Friendship not found")

		friendship.toggleFavorite()
		val saved = friendshipRepository.save(friendship)

		logger.info { "Favorite toggled: favorite=${saved.favorite}" }
		return saved.toResponse()
	}

	/**
	 * 즐겨찾기 친구 목록 조회
	 */
	@Transactional(readOnly = true)
	fun getFavoriteFriends(userId: String): List<FriendshipResponse> {
		logger.debug { "Getting favorite friends for user: $userId" }

		return friendshipRepository.findFavoritesByUserId(UserId.of(userId))
			.map { it.toResponse() }
	}

	// === Private Helper Methods ===

	private fun findUserById(userId: String): User {
		return userRepository.findById(UserId.of(userId))
			?: throw ResourceNotFoundException("User not found: $userId")
	}
}
