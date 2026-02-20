package com.example.chat.message.application.service

import com.example.chat.auth.core.util.SecurityUtils
import com.example.chat.domain.channel.Channel
import com.example.chat.domain.channel.ChannelId
import com.example.chat.domain.channel.ChannelRepository
import com.example.chat.domain.message.Message
import com.example.chat.domain.message.MessageRepository
import com.example.chat.domain.message.MessageType
import com.example.chat.domain.service.MessageDomainService
import com.example.chat.domain.user.User
import com.example.chat.domain.user.UserId
import com.example.chat.domain.user.UserRepository
import com.example.chat.message.application.dto.request.SendMessageRequest
import com.example.chat.message.application.dto.response.MessageResponse
import com.example.chat.message.infrastructure.messaging.MessageEventPublisher
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 메시지 애플리케이션 서비스 (Use Case)
 *
 * Application Service의 역할:
 * 1. 트랜잭션 경계 관리
 * 2. 인증/인가 확인
 * 3. Repository에서 Aggregate 조회
 * 4. Domain Service 호출 (도메인 로직은 Domain Service에 위임)
 * 5. 이벤트 발행
 * 6. DTO 변환
 *
 * 비즈니스 규칙 검증은 Domain Service에서 처리
 */
@Service
@Transactional(readOnly = true)
class MessageApplicationService(
	private val messageRepository: MessageRepository,
	private val channelRepository: ChannelRepository,
	private val userRepository: UserRepository,
	private val messageDomainService: MessageDomainService,
	private val messageEventPublisher: MessageEventPublisher
) {
	private val log = LoggerFactory.getLogger(javaClass)

	/**
	 * 메시지 발송 Use Case
	 *
	 * 흐름:
	 * 1. 인증 확인
	 * 2. Aggregate 조회 (Channel, User)
	 * 3. Domain Service 호출 (비즈니스 규칙 검증 + 메시지 생성)
	 * 4. 저장
	 * 5. 이벤트 발행
	 */
	@Transactional
	fun sendMessage(request: SendMessageRequest): MessageResponse {
		log.info("Sending message: channelId={}, type={}", request.channelId, request.messageType)

		// Step 1: 인증 확인 - 인증된 사용자 ID 조회
		val senderId = getUserIdFromContext()

		// Step 2: 필수 파라미터 검증 (Early return)
		if (request.channelId.isNullOrBlank()) {
			throw IllegalArgumentException("Channel ID is required")
		}

		// Step 3: Aggregate 조회 - Channel
		val channel = findChannelById(request.channelId)

		// Step 4: Aggregate 조회 - User
		val sender = findUserById(senderId)

		// Step 5: Domain Service 호출 - 메시지 생성 (도메인 규칙 검증 포함)
		val message = createMessageByType(channel, sender, request)

		// Step 6: 저장
		val savedMessage = messageRepository.save(message)

		// Step 7: 이벤트 발행 (비동기)
		publishMessageEvent(savedMessage)

		// Step 8: Response 변환
		val response = convertToResponse(savedMessage)

		log.info(
			"Message sent successfully: messageId={}, channelId={}, senderId={}",
			savedMessage.id?.value, channel.id.value, sender.id.value
		)

		return response
	}

	// ========== Private Helper Methods ==========

	/**
	 * 인증된 사용자 ID 조회
	 */
	private fun getUserIdFromContext(): UserId {
		return SecurityUtils.getCurrentUserId()
			.map { UserId.of(it) }
			.orElseThrow { IllegalStateException("User not authenticated") }
	}

	/**
	 * Channel Aggregate 조회
	 */
	private fun findChannelById(channelIdStr: String): Channel {
		val channelId = ChannelId.of(channelIdStr)
		return channelRepository.findById(channelId)
			?: throw IllegalArgumentException("Channel not found: $channelIdStr")
	}

	/**
	 * User Aggregate 조회
	 */
	private fun findUserById(userId: UserId): User {
		return userRepository.findById(userId)
			?: throw IllegalArgumentException("User not found: ${userId.value}")
	}

	/**
	 * MessageType에 따라 메시지 생성
	 *
	 * Domain Service에 Aggregate를 전달하여 도메인 규칙 검증 수행
	 */
	private fun createMessageByType(
		channel: Channel,
		sender: User,
		request: SendMessageRequest
	): Message {
		// Early return: MessageType 검증
		val type = request.messageType ?: throw IllegalArgumentException("Message type is required")

		return when (type) {
			MessageType.TEXT -> {
				val text = extractTextField(request, "text")
				messageDomainService.createTextMessage(channel, sender, text)
			}

			MessageType.IMAGE -> {
				val imageUrl = extractTextField(request, "imageUrl")
				val imageName = extractTextFieldOrDefault(request, "fileName", "image.jpg")
				val imageSize = extractLongFieldOrDefault(request, "fileSize", 0L)
				messageDomainService.createImageMessage(channel, sender, imageUrl, imageName, imageSize)
			}

			MessageType.FILE -> {
				val fileUrl = extractTextField(request, "fileUrl")
				val fileName = extractTextField(request, "fileName")
				val fileSize = extractLongFieldOrDefault(request, "fileSize", 0L)
				val mimeType = extractTextFieldOrDefault(request, "mimeType", "application/octet-stream")
				messageDomainService.createFileMessage(channel, sender, fileUrl, fileName, fileSize, mimeType)
			}

			MessageType.VIDEO -> {
				val videoUrl = extractTextField(request, "videoUrl")
				val fileName = extractTextFieldOrDefault(request, "fileName", "video.mp4")
				val fileSize = extractLongFieldOrDefault(request, "fileSize", 0L)
				val mimeType = extractTextFieldOrDefault(request, "mimeType", "video/mp4")
				messageDomainService.createFileMessage(channel, sender, videoUrl, fileName, fileSize, mimeType)
			}

			MessageType.AUDIO -> {
				val audioUrl = extractTextField(request, "audioUrl")
				val fileName = extractTextFieldOrDefault(request, "fileName", "audio.mp3")
				val fileSize = extractLongFieldOrDefault(request, "fileSize", 0L)
				val mimeType = extractTextFieldOrDefault(request, "mimeType", "audio/mpeg")
				messageDomainService.createFileMessage(channel, sender, audioUrl, fileName, fileSize, mimeType)
			}

			MessageType.SYSTEM -> {
				// 시스템 메시지는 User가 필요 없음
				val systemText = extractTextField(request, "text")
				messageDomainService.createSystemMessage(channel, systemText)
			}
		}
	}

	/**
	 * Payload에서 필수 텍스트 필드 추출
	 */
	private fun extractTextField(request: SendMessageRequest, fieldName: String): String {
		val payload = request.payload ?: throw IllegalArgumentException("Payload is required")
		val value = payload[fieldName] ?: throw IllegalArgumentException("Field '$fieldName' is required in payload")
		return value.toString()
	}

	/**
	 * Payload에서 텍스트 필드 추출 (기본값 있음)
	 */
	private fun extractTextFieldOrDefault(
		request: SendMessageRequest,
		fieldName: String,
		defaultValue: String
	): String {
		val payload = request.payload ?: return defaultValue
		val value = payload[fieldName] ?: return defaultValue
		return value.toString()
	}

	/**
	 * Payload에서 Long 필드 추출 (기본값 있음)
	 */
	private fun extractLongFieldOrDefault(
		request: SendMessageRequest,
		fieldName: String,
		defaultValue: Long
	): Long {
		val payload = request.payload ?: return defaultValue
		val value = payload[fieldName] ?: return defaultValue

		return when (value) {
			is Number -> value.toLong()
			else -> value.toString().toLongOrNull() ?: defaultValue
		}
	}

	/**
	 * 이벤트 발행
	 */
	private fun publishMessageEvent(message: Message) {
		try {
			messageEventPublisher.publishMessageSent(message)
		} catch (e: Exception) {
			log.error("Failed to publish message event: messageId=${message.id?.value}", e)
			// 이벤트 발행 실패는 메시지 발송을 막지 않음
		}
	}

	/**
	 * Message 도메인을 Response DTO로 변환
	 */
	private fun convertToResponse(message: Message): MessageResponse {
		return MessageResponse(
			id = message.id?.value ?: "",
			channelId = message.channelId.value,
			senderId = message.senderId.value,
			messageType = message.type,
			content = message.content.text ?: "",
			status = message.status,
			createdAt = message.createdAt,
			sentAt = message.sentAt
		)
	}
}
