package com.example.chat.domain.service

import com.example.chat.domain.friendship.Friendship
import com.example.chat.domain.user.User
import org.springframework.stereotype.Service

/**
 * 친구 관계 도메인 서비스
 *
 * DDD Domain Service의 역할:
 * - User + Friendship Aggregate 간 협력 조율
 * - 양방향 친구 관계 생성 규칙 캡슐화
 * - 복잡한 도메인 규칙 검증
 */
@Service
class FriendshipDomainService {

	/**
	 * 친구 요청 생성
	 *
	 * Domain Rule:
	 * - 양방향 관계 생성 (A→B, B→A)
	 * - 두 사용자 모두 활성 상태여야 함
	 * - 자기 자신과는 친구 관계를 맺을 수 없음
	 */
	fun requestFriendship(requester: User, target: User): FriendshipPair {
		// Early Return: 자기 자신 체크
		require(requester.id != target.id) {
			throw DomainException("Cannot add yourself as a friend")
		}

		// Early Return: 사용자 상태 체크
		require(requester.canSendMessage()) {
			throw DomainException("Requester is not in active status")
		}
		require(target.canSendMessage()) {
			throw DomainException("Target user is not in active status")
		}

		// 양방향 관계 생성
		val requestToTarget = Friendship.requestFriendship(requester.id, target.id)
		val requestFromTarget = Friendship.requestFriendship(target.id, requester.id)

		return FriendshipPair(requestToTarget, requestFromTarget)
	}

	/**
	 * 친구 요청 수락
	 *
	 * Domain Rule:
	 * - 양방향 모두 ACCEPTED 상태로 변경
	 * - PENDING 상태의 요청만 수락 가능
	 */
	fun acceptFriendship(myRequest: Friendship, theirRequest: Friendship) {
		// Early Return: 상태 검증
		require(myRequest.isPending()) {
			throw DomainException("Can only accept pending requests")
		}
		require(theirRequest.isPending()) {
			throw DomainException("Mutual request is not pending")
		}

		// Early Return: 양방향 관계 검증
		require(
			myRequest.userId == theirRequest.friendId &&
					myRequest.friendId == theirRequest.userId
		) {
			throw DomainException("Invalid mutual friendship relationship")
		}

		// 양방향 수락
		myRequest.accept()
		theirRequest.accept()
	}

	/**
	 * 친구 차단
	 */
	fun blockFriend(friendship: Friendship) {
		friendship.block()
	}

	/**
	 * 친구 차단 해제
	 */
	fun unblockFriend(friendship: Friendship) {
		friendship.unblock()
	}

	/**
	 * 양방향 친구 관계 Pair
	 */
	data class FriendshipPair(
		val first: Friendship,   // 요청자 → 대상
		val second: Friendship   // 대상 → 요청자
	) {
		val requestToTarget: Friendship get() = first
		val requestFromTarget: Friendship get() = second
	}
}
