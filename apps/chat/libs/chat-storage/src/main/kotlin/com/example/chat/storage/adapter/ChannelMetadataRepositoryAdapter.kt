package com.example.chat.storage.adapter

import com.example.chat.domain.channel.ChannelId
import com.example.chat.domain.channel.metadata.ChannelMetadata
import com.example.chat.domain.channel.metadata.ChannelMetadataId
import com.example.chat.domain.channel.metadata.ChannelMetadataRepository
import com.example.chat.domain.user.UserId
import com.example.chat.storage.mapper.ChannelMetadataMapper
import com.example.chat.storage.repository.JpaChannelMetadataRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

/**
 * ChannelMetadataRepository 구현체 (Adapter)
 */
@Repository
class ChannelMetadataRepositoryAdapter(
	private val jpaRepository: JpaChannelMetadataRepository,
	private val mapper: ChannelMetadataMapper
) : ChannelMetadataRepository {

	@Transactional
	override fun save(metadata: ChannelMetadata): ChannelMetadata {
		val entity = mapper.toEntity(metadata)
		val saved = jpaRepository.save(entity)
		return mapper.toDomain(saved)
	}

	@Transactional(readOnly = true)
	override fun findById(id: ChannelMetadataId): ChannelMetadata? {
		return jpaRepository.findById(id.value)
			.orElse(null)
			?.let { mapper.toDomain(it) }
	}

	@Transactional(readOnly = true)
	override fun findByChannelIdAndUserId(channelId: ChannelId, userId: UserId): ChannelMetadata? {
		return jpaRepository.findByChannelIdAndUserId(channelId.value, userId.value)
			?.let { mapper.toDomain(it) }
	}

	@Transactional(readOnly = true)
	override fun findByUserId(userId: UserId): List<ChannelMetadata> {
		return jpaRepository.findByUserId(userId.value)
			.map { mapper.toDomain(it) }
	}

	@Transactional(readOnly = true)
	override fun findByChannelIdsAndUserId(
		channelIds: List<ChannelId>,
		userId: UserId
	): Map<ChannelId, ChannelMetadata> {
		val channelIdStrings = channelIds.map { it.value }

		return jpaRepository.findByChannelIdsAndUserId(channelIdStrings, userId.value)
			.map { mapper.toDomain(it) }
			.associateBy { it.channelId }
	}

	@Transactional(readOnly = true)
	override fun findFavoritesByUserId(userId: UserId): List<ChannelMetadata> {
		return jpaRepository.findFavoritesByUserId(userId.value)
			.map { mapper.toDomain(it) }
	}

	@Transactional(readOnly = true)
	override fun findPinnedByUserId(userId: UserId): List<ChannelMetadata> {
		return jpaRepository.findPinnedByUserId(userId.value)
			.map { mapper.toDomain(it) }
	}

	@Transactional(readOnly = true)
	override fun findWithUnreadByUserId(userId: UserId): List<ChannelMetadata> {
		return jpaRepository.findWithUnreadByUserId(userId.value)
			.map { mapper.toDomain(it) }
	}

	@Transactional
	override fun deleteById(id: ChannelMetadataId) {
		jpaRepository.deleteById(id.value)
	}

	@Transactional
	override fun deleteByChannelId(channelId: ChannelId) {
		jpaRepository.deleteByChannelId(channelId.value)
	}

	@Transactional(readOnly = true)
	override fun existsByChannelIdAndUserId(channelId: ChannelId, userId: UserId): Boolean {
		return jpaRepository.existsByChannelIdAndUserId(channelId.value, userId.value)
	}
}
