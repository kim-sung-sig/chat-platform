package com.example.chat.storage.repository

import com.example.chat.domain.friendship.FriendshipStatus
import com.example.chat.storage.entity.ChatFriendshipEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

/**
 * 친구 관계 JPA Repository
 */
interface JpaFriendshipRepository : JpaRepository<ChatFriendshipEntity, String> {

	/**
	 * 두 사용자 간 친구 관계 조회
	 */
	fun findByUserIdAndFriendId(userId: String, friendId: String): ChatFriendshipEntity?

	/**
	 * 사용자의 수락된 친구 목록 조회
	 */
	@Query("SELECT f FROM ChatFriendshipEntity f WHERE f.userId = :userId AND f.status = 'ACCEPTED' ORDER BY f.favorite DESC, f.updatedAt DESC")
	fun findAcceptedFriendsByUserId(userId: String): List<ChatFriendshipEntity>

	/**
	 * 사용자에게 온 친구 요청 목록 조회
	 */
	@Query("SELECT f FROM ChatFriendshipEntity f WHERE f.friendId = :friendId AND f.status = 'PENDING' ORDER BY f.createdAt DESC")
	fun findPendingRequestsByFriendId(friendId: String): List<ChatFriendshipEntity>

	/**
	 * 사용자가 보낸 친구 요청 목록 조회
	 */
	@Query("SELECT f FROM ChatFriendshipEntity f WHERE f.userId = :userId AND f.status = 'PENDING' ORDER BY f.createdAt DESC")
	fun findPendingRequestsByUserId(userId: String): List<ChatFriendshipEntity>

	/**
	 * 사용자가 차단한 목록 조회
	 */
	@Query("SELECT f FROM ChatFriendshipEntity f WHERE f.userId = :userId AND f.status = 'BLOCKED' ORDER BY f.updatedAt DESC")
	fun findBlockedByUserId(userId: String): List<ChatFriendshipEntity>

	/**
	 * 즐겨찾기 친구 목록 조회
	 */
	@Query("SELECT f FROM ChatFriendshipEntity f WHERE f.userId = :userId AND f.status = 'ACCEPTED' AND f.favorite = true ORDER BY f.updatedAt DESC")
	fun findFavoritesByUserId(userId: String): List<ChatFriendshipEntity>

	/**
	 * 양방향 친구 관계 존재 여부 확인
	 */
	@Query("SELECT COUNT(f) > 0 FROM ChatFriendshipEntity f WHERE (f.userId = :userId AND f.friendId = :friendId) OR (f.userId = :friendId AND f.friendId = :userId)")
	fun existsMutualFriendship(userId: String, friendId: String): Boolean

	/**
	 * 사용자의 모든 친구 관계 조회 (상태 무관)
	 */
	fun findAllByUserId(userId: String): List<ChatFriendshipEntity>

	/**
	 * 상태별 친구 관계 조회
	 */
	fun findByUserIdAndStatus(userId: String, status: FriendshipStatus): List<ChatFriendshipEntity>
}
