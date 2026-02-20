package com.example.chat.system.application.service

import com.example.chat.domain.channel.ChannelId
import com.example.chat.domain.channel.ChannelRepository
import com.example.chat.domain.channel.metadata.ChannelMetadata
import com.example.chat.domain.channel.metadata.ChannelMetadataRepository
import com.example.chat.domain.message.MessageId
import com.example.chat.domain.service.DomainException
import com.example.chat.domain.user.UserId
import com.example.chat.system.application.dto.response.ChannelMetadataResponse
import com.example.chat.system.application.dto.response.toResponse
import com.example.chat.system.exception.ResourceNotFoundException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {}

/**
 * 채팅방 메타데이터 Application Service
 */
@Service
@Transactional
class ChannelMetadataApplicationService(
	private val metadataRepository: ChannelMetadataRepository,
	private val channelRepository: ChannelRepository
) {

	fun getOrCreateMetadata(userId: String, channelId: String): ChannelMetadataResponse {
		logger.debug { "Getting or creating metadata: userId=$userId, channelId=$channelId" }

		val cid = ChannelId.of(channelId)
		val uid = UserId.of(userId)

		// 채널 존재 및 멤버 확인
		val channel = channelRepository.findById(cid)
			?: throw ResourceNotFoundException("Channel not found: $channelId")

		require(channel.isMember(uid)) {
			throw DomainException("User is not a member of the channel")
		}

		// 메타데이터 조회 또는 생성
		val metadata = metadataRepository.findByChannelIdAndUserId(cid, uid)
			?: ChannelMetadata.create(cid, uid).also { metadataRepository.save(it) }

		return metadata.toResponse()
	}

	fun markAsRead(userId: String, channelId: String, messageId: String): ChannelMetadataResponse {
		logger.info { "Marking as read: userId=$userId, channelId=$channelId, messageId=$messageId" }

		val metadata = findMetadata(userId, channelId)
		metadata.markAsRead(MessageId.of(messageId))

		return metadataRepository.save(metadata).toResponse()
	}

	fun incrementUnreadCount(userId: String, channelId: String) {
		logger.debug { "Incrementing unread count: userId=$userId, channelId=$channelId" }

		val metadata = metadataRepository.findByChannelIdAndUserId(
			ChannelId.of(channelId),
			UserId.of(userId)
		) ?: ChannelMetadata.create(ChannelId.of(channelId), UserId.of(userId))

		metadata.incrementUnreadCount()
		metadataRepository.save(metadata)
	}

	fun toggleNotification(userId: String, channelId: String): ChannelMetadataResponse {
		logger.info { "Toggling notification: userId=$userId, channelId=$channelId" }

		val metadata = findMetadata(userId, channelId)
		metadata.toggleNotification()

		return metadataRepository.save(metadata).toResponse()
	}

	fun toggleFavorite(userId: String, channelId: String): ChannelMetadataResponse {
		logger.info { "Toggling favorite: userId=$userId, channelId=$channelId" }

		val metadata = findMetadata(userId, channelId)
		metadata.toggleFavorite()

		return metadataRepository.save(metadata).toResponse()
	}

	fun togglePinned(userId: String, channelId: String): ChannelMetadataResponse {
		logger.info { "Toggling pinned: userId=$userId, channelId=$channelId" }

		val metadata = findMetadata(userId, channelId)
		metadata.togglePinned()

		return metadataRepository.save(metadata).toResponse()
	}

	@Transactional(readOnly = true)
	fun getFavorites(userId: String): List<ChannelMetadataResponse> {
		logger.debug { "Getting favorites for user: $userId" }

		return metadataRepository.findFavoritesByUserId(UserId.of(userId))
			.map { it.toResponse() }
	}

	@Transactional(readOnly = true)
	fun getPinned(userId: String): List<ChannelMetadataResponse> {
		logger.debug { "Getting pinned channels for user: $userId" }

		return metadataRepository.findPinnedByUserId(UserId.of(userId))
			.map { it.toResponse() }
	}

	@Transactional(readOnly = true)
	fun getWithUnread(userId: String): List<ChannelMetadataResponse> {
		logger.debug { "Getting channels with unread messages for user: $userId" }

		return metadataRepository.findWithUnreadByUserId(UserId.of(userId))
			.map { it.toResponse() }
	}

	private fun findMetadata(userId: String, channelId: String): ChannelMetadata {
		return metadataRepository.findByChannelIdAndUserId(
			ChannelId.of(channelId),
			UserId.of(userId)
		) ?: throw ResourceNotFoundException("Channel metadata not found")
	}
}
