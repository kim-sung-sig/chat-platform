package com.example.chat.storage.adapter

import com.example.chat.domain.friendship.Friendship
import com.example.chat.domain.friendship.FriendshipId
import com.example.chat.domain.friendship.FriendshipRepository
import com.example.chat.domain.user.UserId
import com.example.chat.storage.mapper.FriendshipMapper
import com.example.chat.storage.repository.JpaFriendshipRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

/**
 * FriendshipRepository 구현체 (Adapter)
 *
 * Hexagonal Architecture의 Adapter
 */
@Repository
class FriendshipRepositoryAdapter(
	private val jpaRepository: JpaFriendshipRepository,
	private val mapper: FriendshipMapper
) : FriendshipRepository {

	@Transactional
	override fun save(friendship: Friendship): Friendship {
		val entity = mapper.toEntity(friendship)
		val saved = jpaRepository.save(entity)
		return mapper.toDomain(saved)
	}

	@Transactional(readOnly = true)
	override fun findById(id: FriendshipId): Friendship? {
		return jpaRepository.findById(id.value)
			.orElse(null)
			?.let { mapper.toDomain(it) }
	}

	@Transactional(readOnly = true)
	override fun findByUserIdAndFriendId(userId: UserId, friendId: UserId): Friendship? {
		return jpaRepository.findByUserIdAndFriendId(userId.value, friendId.value)
			?.let { mapper.toDomain(it) }
	}

	@Transactional(readOnly = true)
	override fun findAcceptedFriendsByUserId(userId: UserId): List<Friendship> {
		return jpaRepository.findAcceptedFriendsByUserId(userId.value)
			.map { mapper.toDomain(it) }
	}

	@Transactional(readOnly = true)
	override fun findPendingRequestsByFriendId(friendId: UserId): List<Friendship> {
		return jpaRepository.findPendingRequestsByFriendId(friendId.value)
			.map { mapper.toDomain(it) }
	}

	@Transactional(readOnly = true)
	override fun findPendingRequestsByUserId(userId: UserId): List<Friendship> {
		return jpaRepository.findPendingRequestsByUserId(userId.value)
			.map { mapper.toDomain(it) }
	}

	@Transactional(readOnly = true)
	override fun findBlockedByUserId(userId: UserId): List<Friendship> {
		return jpaRepository.findBlockedByUserId(userId.value)
			.map { mapper.toDomain(it) }
	}

	@Transactional(readOnly = true)
	override fun findFavoritesByUserId(userId: UserId): List<Friendship> {
		return jpaRepository.findFavoritesByUserId(userId.value)
			.map { mapper.toDomain(it) }
	}

	@Transactional
	override fun deleteById(id: FriendshipId) {
		jpaRepository.deleteById(id.value)
	}

	@Transactional(readOnly = true)
	override fun existsMutualFriendship(userId: UserId, friendId: UserId): Boolean {
		return jpaRepository.existsMutualFriendship(userId.value, friendId.value)
	}

	@Transactional(readOnly = true)
	override fun findAllByUserId(userId: UserId): List<Friendship> {
		return jpaRepository.findAllByUserId(userId.value)
			.map { mapper.toDomain(it) }
	}
}
