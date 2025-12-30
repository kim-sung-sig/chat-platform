package com.example.chat.system.controller;

import com.example.chat.system.application.service.MessageQueryService;
import com.example.chat.system.dto.response.ApiResponse;
import com.example.chat.system.dto.response.CursorPageResponse;
import com.example.chat.system.dto.response.MessageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 메시지 조회 REST Controller
 *
 * CQRS 패턴:
 * - Command: MessageController (chat-message-server)
 * - Query: MessageQueryController (chat-system-server)
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
@Tag(name = "Message Query", description = "메시지 조회 API")
public class MessageQueryController {

	private final MessageQueryService messageQueryService;

	/**
	 * 채널의 메시지 목록 조회 (커서 기반 페이징)
	 *
	 * @param channelId 채널 ID (필수)
	 * @param cursor 커서 (선택, 없으면 첫 페이지)
	 * @param limit 조회할 메시지 수 (선택, 기본값: 20, 최대: 100)
	 * @return 커서 페이징 응답
	 */
	@GetMapping
	@Operation(
		summary = "메시지 목록 조회 (커서 페이징)",
		description = "채널의 메시지를 커서 기반 페이징으로 조회합니다. 최신 메시지부터 조회됩니다."
	)
	public ResponseEntity<ApiResponse<CursorPageResponse<MessageResponse>>> getMessages(
			@Parameter(description = "채널 ID", required = true)
			@RequestParam String channelId,

			@Parameter(description = "커서 (Base64 인코딩, 없으면 첫 페이지)", required = false)
			@RequestParam(required = false) String cursor,

			@Parameter(description = "조회할 메시지 수 (기본: 20, 최대: 100)", required = false)
			@RequestParam(required = false) Integer limit) {

		log.info("GET /api/v1/messages - channelId={}, cursor={}, limit={}",
				channelId, cursor, limit);

		CursorPageResponse<MessageResponse> response = messageQueryService.getMessages(channelId, cursor, limit);

		return ResponseEntity.ok(ApiResponse.success(response));
	}

	/**
	 * 특정 메시지 조회
	 *
	 * @param messageId 메시지 ID
	 * @return 메시지 응답
	 */
	@GetMapping("/{messageId}")
	@Operation(
		summary = "메시지 조회",
		description = "메시지 ID로 특정 메시지를 조회합니다"
	)
	public ResponseEntity<ApiResponse<MessageResponse>> getMessage(
			@Parameter(description = "메시지 ID", required = true)
			@PathVariable String messageId) {

		log.info("GET /api/v1/messages/{} - messageId={}", messageId, messageId);

		MessageResponse response = messageQueryService.getMessage(messageId);

		return ResponseEntity.ok(ApiResponse.success(response));
	}

	/**
	 * 읽지 않은 메시지 수 조회
	 *
	 * @param channelId 채널 ID
	 * @return 읽지 않은 메시지 수
	 */
	@GetMapping("/unread-count")
	@Operation(
		summary = "읽지 않은 메시지 수 조회",
		description = "채널의 읽지 않은 메시지 수를 조회합니다 (TODO: 실제 구현 필요)"
	)
	public ResponseEntity<ApiResponse<Long>> getUnreadMessageCount(
			@Parameter(description = "채널 ID", required = true)
			@RequestParam String channelId) {

		log.info("GET /api/v1/messages/unread-count - channelId={}", channelId);

		long count = messageQueryService.getUnreadMessageCount(channelId);

		return ResponseEntity.ok(ApiResponse.success(count));
	}
}
