package com.example.chat.message.presentation.controller;

import com.example.chat.message.application.dto.request.SendMessageRequest;
import com.example.chat.message.application.dto.response.CursorPageResponse;
import com.example.chat.message.application.dto.response.MessageResponse;
import com.example.chat.message.application.service.MessageQueryService;
import com.example.chat.message.application.service.MessageSendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 메시지 컨트롤러
 *
 * 책임: HTTP 요청/응답, Validation, Service 위임
 * - POST /api/messages          → 메시지 발송
 * - GET  /api/messages/{channelId} → cursor 기반 메시지 조회
 */
@Tag(name = "Message", description = "메시지 API")
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Slf4j
public class MessageController {

    private final MessageSendService messageSendService;
    private final MessageQueryService messageQueryService;

    @Operation(summary = "메시지 발송",
            description = "채팅방에 메시지를 발송합니다. 텍스트/이미지/파일/시스템 타입 지원.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "메시지 발송 성공",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "채널 접근 권한 없음")
    })
    @PostMapping
    public ResponseEntity<MessageResponse> sendMessage(@Valid @RequestBody SendMessageRequest request) {
        log.info("POST /api/messages - channelId={}, type={}", request.channelId(), request.messageType());
        return ResponseEntity.status(HttpStatus.CREATED).body(messageSendService.sendMessage(request));
    }

    @Operation(summary = "채널 메시지 조회",
            description = "cursor 기반 페이징으로 채널 메시지를 최신순으로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "채널 접근 권한 없음"),
            @ApiResponse(responseCode = "404", description = "채널 없음")
    })
    @GetMapping("/{channelId}")
    public ResponseEntity<CursorPageResponse<MessageResponse>> getMessages(
            @PathVariable String channelId,
            @Parameter(description = "이전 페이지 마지막 메시지 createdAt (ISO-8601). null이면 최신부터 조회")
            @RequestParam(required = false) String cursor,
            @Parameter(description = "페이지 크기 (기본 50, 최대 100)")
            @RequestParam(defaultValue = "50") int limit) {
        log.debug("GET /api/messages/{} cursor={} limit={}", channelId, cursor, limit);
        return ResponseEntity.ok(messageQueryService.getMessages(channelId, cursor, limit));
    }

    @Operation(summary = "Health Check")
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}
