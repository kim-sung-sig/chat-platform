package com.example.chat.system.controller

import com.example.chat.system.application.dto.response.ChannelMetadataResponse
import com.example.chat.system.application.service.ChannelMetadataApplicationService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/api/channels")
@Tag(name = "Channel Metadata", description = "채팅방 메타데이터 API")
class ChannelMetadataController(
	private val metadataService: ChannelMetadataApplicationService
) {

	@GetMapping("/{channelId}/metadata")
	@Operation(summary = "메타데이터 조회/생성")
	fun getOrCreateMetadata(
		@RequestHeader("X-User-Id") userId: String,
		@PathVariable channelId: String
	): ResponseEntity<ChannelMetadataResponse> {
		logger.info { "GET /api/channels/$channelId/metadata - userId: $userId" }

		val response = metadataService.getOrCreateMetadata(userId, channelId)
		return ResponseEntity.ok(response)
	}

	@PutMapping("/{channelId}/read")
	@Operation(summary = "읽음 처리")
	fun markAsRead(
		@RequestHeader("X-User-Id") userId: String,
		@PathVariable channelId: String,
		@RequestParam messageId: String
	): ResponseEntity<ChannelMetadataResponse> {
		logger.info { "PUT /api/channels/$channelId/read - userId: $userId, messageId: $messageId" }

		val response = metadataService.markAsRead(userId, channelId, messageId)
		return ResponseEntity.ok(response)
	}

	@PutMapping("/{channelId}/notification")
	@Operation(summary = "알림 설정 토글")
	fun toggleNotification(
		@RequestHeader("X-User-Id") userId: String,
		@PathVariable channelId: String
	): ResponseEntity<ChannelMetadataResponse> {
		logger.info { "PUT /api/channels/$channelId/notification - userId: $userId" }

		val response = metadataService.toggleNotification(userId, channelId)
		return ResponseEntity.ok(response)
	}

	@PutMapping("/{channelId}/favorite")
	@Operation(summary = "즐겨찾기 토글")
	fun toggleFavorite(
		@RequestHeader("X-User-Id") userId: String,
		@PathVariable channelId: String
	): ResponseEntity<ChannelMetadataResponse> {
		logger.info { "PUT /api/channels/$channelId/favorite - userId: $userId" }

		val response = metadataService.toggleFavorite(userId, channelId)
		return ResponseEntity.ok(response)
	}

	@PutMapping("/{channelId}/pin")
	@Operation(summary = "상단 고정 토글")
	fun togglePinned(
		@RequestHeader("X-User-Id") userId: String,
		@PathVariable channelId: String
	): ResponseEntity<ChannelMetadataResponse> {
		logger.info { "PUT /api/channels/$channelId/pin - userId: $userId" }

		val response = metadataService.togglePinned(userId, channelId)
		return ResponseEntity.ok(response)
	}

	@GetMapping("/favorites")
	@Operation(summary = "즐겨찾기 채팅방")
	fun getFavorites(
		@RequestHeader("X-User-Id") userId: String
	): ResponseEntity<List<ChannelMetadataResponse>> {
		logger.info { "GET /api/channels/favorites - userId: $userId" }

		val favorites = metadataService.getFavorites(userId)
		return ResponseEntity.ok(favorites)
	}

	@GetMapping("/pinned")
	@Operation(summary = "상단 고정 채팅방")
	fun getPinned(
		@RequestHeader("X-User-Id") userId: String
	): ResponseEntity<List<ChannelMetadataResponse>> {
		logger.info { "GET /api/channels/pinned - userId: $userId" }

		val pinned = metadataService.getPinned(userId)
		return ResponseEntity.ok(pinned)
	}

	@GetMapping("/unread")
	@Operation(summary = "읽지 않은 메시지가 있는 채팅방")
	fun getWithUnread(
		@RequestHeader("X-User-Id") userId: String
	): ResponseEntity<List<ChannelMetadataResponse>> {
		logger.info { "GET /api/channels/unread - userId: $userId" }

		val unread = metadataService.getWithUnread(userId)
		return ResponseEntity.ok(unread)
	}
}
