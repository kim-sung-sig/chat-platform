package com.example.chat.message.presentation.controller

import com.example.chat.message.application.dto.request.SendMessageRequest
import com.example.chat.message.application.dto.response.MessageResponse
import com.example.chat.message.application.service.MessageApplicationService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * 메시지 컨트롤러
 *
 * 책임:
 * - HTTP 요청 수신 및 응답
 * - Validation 검증 (@Valid)
 * - Application Service 호출
 *
 * 비즈니스 로직은 Application Service에 위임
 */
@Tag(name = "Message", description = "메시지 발송 API")
@RestController
@RequestMapping("/api/messages")
class MessageController(
	private val messageApplicationService: MessageApplicationService
) {
	private val log = LoggerFactory.getLogger(javaClass)

	/**
	 * 메시지 발송
	 */
	@Operation(
		summary = "메시지 발송",
		description = "채팅방에 메시지를 발송합니다. 텍스트, 이미지, 파일, 시스템 메시지 타입을 지원합니다."
	)
	@ApiResponses(
		ApiResponse(
			responseCode = "201",
			description = "메시지 발송 성공",
			content = [Content(schema = Schema(implementation = MessageResponse::class))]
		),
		ApiResponse(responseCode = "400", description = "잘못된 요청"),
		ApiResponse(responseCode = "401", description = "인증 실패"),
		ApiResponse(responseCode = "500", description = "서버 오류")
	)
	@PostMapping
	fun sendMessage(
		@Valid @RequestBody request: SendMessageRequest
	): ResponseEntity<MessageResponse> {
		log.info(
			"POST /api/messages - channelId: {}, type: {}",
			request.channelId, request.messageType
		)

		val response = messageApplicationService.sendMessage(request)

		return ResponseEntity
			.status(HttpStatus.CREATED)
			.body(response)
	}

	/**
	 * Health Check
	 */
	@Operation(
		summary = "Health Check",
		description = "서버 상태를 확인합니다."
	)
	@ApiResponse(responseCode = "200", description = "서버 정상")
	@GetMapping("/health")
	fun health(): ResponseEntity<String> {
		return ResponseEntity.ok("OK")
	}
}
