package com.example.chat.system.controller

import com.example.chat.system.application.dto.request.FriendshipRequest
import com.example.chat.system.application.dto.request.SetNicknameRequest
import com.example.chat.system.application.dto.response.FriendshipResponse
import com.example.chat.system.application.service.FriendshipApplicationService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/api/friendships")
@Tag(name = "Friendship", description = "친구 관리 API")
class FriendshipController(
	private val friendshipService: FriendshipApplicationService
) {

	@PostMapping
	@Operation(summary = "친구 요청")
	fun requestFriendship(
		@RequestHeader("X-User-Id") userId: String,
		@Valid @RequestBody request: FriendshipRequest
	): ResponseEntity<FriendshipResponse> {
		logger.info { "POST /api/friendships - userId: $userId, friendId: ${request.friendId}" }

		val response = friendshipService.requestFriendship(userId, request.friendId)
		return ResponseEntity.status(HttpStatus.CREATED).body(response)
	}

	@GetMapping
	@Operation(summary = "친구 목록 조회")
	fun getFriendList(
		@RequestHeader("X-User-Id") userId: String
	): ResponseEntity<List<FriendshipResponse>> {
		logger.info { "GET /api/friendships - userId: $userId" }

		val friends = friendshipService.getFriendList(userId)
		return ResponseEntity.ok(friends)
	}

	@GetMapping("/pending")
	@Operation(summary = "받은 친구 요청 목록")
	fun getPendingRequests(
		@RequestHeader("X-User-Id") userId: String
	): ResponseEntity<List<FriendshipResponse>> {
		logger.info { "GET /api/friendships/pending - userId: $userId" }

		val requests = friendshipService.getPendingRequests(userId)
		return ResponseEntity.ok(requests)
	}

	@GetMapping("/sent")
	@Operation(summary = "보낸 친구 요청 목록")
	fun getSentRequests(
		@RequestHeader("X-User-Id") userId: String
	): ResponseEntity<List<FriendshipResponse>> {
		logger.info { "GET /api/friendships/sent - userId: $userId" }

		val requests = friendshipService.getSentRequests(userId)
		return ResponseEntity.ok(requests)
	}

	@GetMapping("/favorites")
	@Operation(summary = "즐겨찾기 친구 목록")
	fun getFavoriteFriends(
		@RequestHeader("X-User-Id") userId: String
	): ResponseEntity<List<FriendshipResponse>> {
		logger.info { "GET /api/friendships/favorites - userId: $userId" }

		val favorites = friendshipService.getFavoriteFriends(userId)
		return ResponseEntity.ok(favorites)
	}

	@PutMapping("/{requestId}/accept")
	@Operation(summary = "친구 요청 수락")
	fun acceptFriendRequest(
		@RequestHeader("X-User-Id") userId: String,
		@PathVariable requestId: String
	): ResponseEntity<FriendshipResponse> {
		logger.info { "PUT /api/friendships/$requestId/accept - userId: $userId" }

		val response = friendshipService.acceptFriendRequest(userId, requestId)
		return ResponseEntity.ok(response)
	}

	@DeleteMapping("/{requestId}/reject")
	@Operation(summary = "친구 요청 거절")
	fun rejectFriendRequest(
		@RequestHeader("X-User-Id") userId: String,
		@PathVariable requestId: String
	): ResponseEntity<Void> {
		logger.info { "DELETE /api/friendships/$requestId/reject - userId: $userId" }

		friendshipService.rejectFriendRequest(userId, requestId)
		return ResponseEntity.noContent().build()
	}

	@DeleteMapping("/users/{friendId}")
	@Operation(summary = "친구 삭제")
	fun deleteFriend(
		@RequestHeader("X-User-Id") userId: String,
		@PathVariable friendId: String
	): ResponseEntity<Void> {
		logger.info { "DELETE /api/friendships/users/$friendId - userId: $userId" }

		friendshipService.deleteFriend(userId, friendId)
		return ResponseEntity.noContent().build()
	}

	@PostMapping("/users/{friendId}/block")
	@Operation(summary = "친구 차단")
	fun blockFriend(
		@RequestHeader("X-User-Id") userId: String,
		@PathVariable friendId: String
	): ResponseEntity<FriendshipResponse> {
		logger.info { "POST /api/friendships/users/$friendId/block - userId: $userId" }

		val response = friendshipService.blockFriend(userId, friendId)
		return ResponseEntity.ok(response)
	}

	@DeleteMapping("/users/{friendId}/block")
	@Operation(summary = "친구 차단 해제")
	fun unblockFriend(
		@RequestHeader("X-User-Id") userId: String,
		@PathVariable friendId: String
	): ResponseEntity<FriendshipResponse> {
		logger.info { "DELETE /api/friendships/users/$friendId/block - userId: $userId" }

		val response = friendshipService.unblockFriend(userId, friendId)
		return ResponseEntity.ok(response)
	}

	@PutMapping("/users/{friendId}/nickname")
	@Operation(summary = "친구 별칭 설정")
	fun setFriendNickname(
		@RequestHeader("X-User-Id") userId: String,
		@PathVariable friendId: String,
		@Valid @RequestBody request: SetNicknameRequest
	): ResponseEntity<FriendshipResponse> {
		logger.info { "PUT /api/friendships/users/$friendId/nickname - userId: $userId" }

		val response = friendshipService.setFriendNickname(userId, friendId, request.nickname)
		return ResponseEntity.ok(response)
	}

	@PutMapping("/users/{friendId}/favorite")
	@Operation(summary = "즐겨찾기 토글")
	fun toggleFavorite(
		@RequestHeader("X-User-Id") userId: String,
		@PathVariable friendId: String
	): ResponseEntity<FriendshipResponse> {
		logger.info { "PUT /api/friendships/users/$friendId/favorite - userId: $userId" }

		val response = friendshipService.toggleFavorite(userId, friendId)
		return ResponseEntity.ok(response)
	}
}
