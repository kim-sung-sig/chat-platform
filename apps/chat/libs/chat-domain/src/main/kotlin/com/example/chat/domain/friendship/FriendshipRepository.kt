package com.example.chat.domain.friendship

import com.example.chat.domain.user.UserId

/**
 * 친구 관계 Repository 인터페이스 (포트)
 *
 * Hexagonal Architecture의 Port
 */
interface FriendshipRepository {

	/**
	 * 친구 관계 저장
	 */
	fun save(friendship: Friendship): Friendship

	/**
	 * ID로 친구 관계 조회
	 */
	fun findById(id: FriendshipId): Friendship?

	/**
	 * 두 사용자 간 친구 관계 조회
	 */
	fun findByUserIdAndFriendId(userId: UserId, friendId: UserId): Friendship?

	/**
	 * 사용자의 모든 친구 목록 조회 (수락된 관계만)
	 */
	fun findAcceptedFriendsByUserId(userId: UserId): List<Friendship>

	/**
	 * 사용자에게 온 친구 요청 목록 조회
	 */
	fun findPendingRequestsByFriendId(friendId: UserId): List<Friendship>

	/**
	 * 사용자가 보낸 친구 요청 목록 조회
	 */
	fun findPendingRequestsByUserId(userId: UserId): List<Friendship>

	/**
	 * 사용자가 차단한 목록 조회
	 */
	fun findBlockedByUserId(userId: UserId): List<Friendship>

	/**
	 * 즐겨찾기 친구 목록 조회
	 */
	fun findFavoritesByUserId(userId: UserId): List<Friendship>

	/**
	 * 친구 관계 삭제
	 */
	fun deleteById(id: FriendshipId)

	/**
	 * 양방향 친구 관계 존재 여부 확인
	 */
	fun existsMutualFriendship(userId: UserId, friendId: UserId): Boolean

	/**
	 * 사용자의 모든 친구 관계 조회 (상태 무관)
	 */
	fun findAllByUserId(userId: UserId): List<Friendship>
}
