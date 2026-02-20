package com.example.chat.domain.friendship

import com.example.chat.domain.service.DomainException
import com.example.chat.domain.user.UserId
import java.time.Instant

/**
 * 친구 관계 Aggregate Root
 *
 * 도메인 규칙:
 * - 친구 관계는 양방향 (A-B 관계 생성 시 B-A도 자동 생성)
 * - 상태: PENDING, ACCEPTED, BLOCKED
 * - 차단은 일방적 가능
 * - 자기 자신과는 친구 관계를 맺을 수 없음
 */
data class Friendship(
	val id: FriendshipId,
	val userId: UserId,        // 관계 요청자 (또는 소유자)
	val friendId: UserId,      // 친구 (대상자)
	val createdAt: Instant,
	var status: FriendshipStatus,    // PENDING, ACCEPTED, BLOCKED
	var nickname: String? = null,    // 친구 별칭 (선택)
	var favorite: Boolean = false,   // 즐겨찾기 여부
	var updatedAt: Instant
) {
	companion object {
		/**
		 * 친구 요청 생성
		 */
		fun requestFriendship(userId: UserId, friendId: UserId): Friendship {
			// Early Return: 자기 자신 체크
			require(userId != friendId) {
				throw DomainException("Cannot add yourself as a friend")
			}

			val now = Instant.now()
			return Friendship(
				id = FriendshipId.generate(),
				userId = userId,
				friendId = friendId,
				status = FriendshipStatus.PENDING,
				nickname = null,
				favorite = false,
				createdAt = now,
				updatedAt = now
			)
		}
	}

	// === Business Methods ===

	/**
	 * 친구 요청 수락
	 */
	fun accept() {
		// Early Return: 상태 검증
		require(status == FriendshipStatus.PENDING) {
			throw DomainException("Only pending requests can be accepted")
		}

		status = FriendshipStatus.ACCEPTED
		updatedAt = Instant.now()
	}

	/**
	 * 친구 요청 거절
	 */
	fun reject() {
		// Early Return: 상태 검증
		require(status == FriendshipStatus.PENDING) {
			throw DomainException("Only pending requests can be rejected")
		}
		// 거절은 관계 자체를 삭제하는 것으로 처리 (Repository에서 delete)
	}

	/**
	 * 친구 차단
	 */
	fun block() {
		status = FriendshipStatus.BLOCKED
		updatedAt = Instant.now()
	}

	/**
	 * 친구 차단 해제
	 */
	fun unblock() {
		// Early Return: 차단 상태만 해제 가능
		require(status == FriendshipStatus.BLOCKED) {
			throw DomainException("Only blocked relationships can be unblocked")
		}

		status = FriendshipStatus.ACCEPTED
		updatedAt = Instant.now()
	}

	/**
	 * 친구 별칭 설정
	 */
	fun updateNickname(nickname: String) {
		// Early Return: 수락된 친구 관계만 별칭 설정 가능
		require(isAccepted()) {
			throw DomainException("Can only set nickname for accepted friends")
		}

		this.nickname = nickname
		updatedAt = Instant.now()
	}

	/**
	 * 즐겨찾기 토글
	 */
	fun toggleFavorite() {
		// Early Return: 수락된 친구 관계만 즐겨찾기 가능
		require(isAccepted()) {
			throw DomainException("Can only favorite accepted friends")
		}

		favorite = !favorite
		updatedAt = Instant.now()
	}

	// === Query Methods ===

	/**
	 * 수락된 친구 관계인지 확인
	 */
	fun isAccepted(): Boolean = status == FriendshipStatus.ACCEPTED

	/**
	 * 대기 중인 요청인지 확인
	 */
	fun isPending(): Boolean = status == FriendshipStatus.PENDING

	/**
	 * 차단된 관계인지 확인
	 */
	fun isBlocked(): Boolean = status == FriendshipStatus.BLOCKED

	/**
	 * 즐겨찾기 친구인지 확인
	 */
	fun isFavorite(): Boolean = favorite && isAccepted()
}
