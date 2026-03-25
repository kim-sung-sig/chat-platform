package com.example.chat.scheduled.rest.controller;

import com.example.chat.auth.core.util.SecurityUtils;
import com.example.chat.scheduled.application.service.ScheduledMessageCommandService;
import com.example.chat.scheduled.application.service.ScheduledMessageQueryService;
import com.example.chat.scheduled.rest.dto.request.CreateScheduledMessageRequest;
import com.example.chat.scheduled.rest.dto.response.ScheduledMessageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 예약 메시지 REST 컨트롤러
 *
 * POST   /api/messages/schedule
 * GET    /api/messages/schedule/{channelId}
 * DELETE /api/messages/schedule/{id}
 */
@Tag(name = "Scheduled Message", description = "메시지 예약 발송 API")
@RestController
@RequestMapping("/api/messages/schedule")
@RequiredArgsConstructor
@Slf4j
public class ScheduledMessageController {

    private final ScheduledMessageCommandService commandService;
    private final ScheduledMessageQueryService queryService;

    @Operation(summary = "메시지 예약 생성", description = "지정 시각에 메시지를 발송 예약합니다. (now+5분 ~ now+30일)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "예약 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (시간 범위 오류, 한도 초과)"),
            @ApiResponse(responseCode = "403", description = "채널 미가입")
    })
    @PostMapping
    public ResponseEntity<ScheduledMessageResponse> createSchedule(
            @Valid @RequestBody CreateScheduledMessageRequest request) {
        String senderId = currentUserId();
        log.info("POST /api/messages/schedule - channelId={}, senderId={}", request.channelId(), senderId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commandService.createScheduledMessage(senderId, request));
    }

    @Operation(summary = "채널 예약 목록 조회", description = "본인의 예약 메시지 목록을 채널 기준으로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "채널 미가입")
    })
    @GetMapping("/{channelId}")
    public ResponseEntity<List<ScheduledMessageResponse>> listSchedules(
            @Parameter(description = "채널 ID") @PathVariable String channelId) {
        String senderId = currentUserId();
        return ResponseEntity.ok(queryService.listScheduledMessages(channelId, senderId));
    }

    @Operation(summary = "예약 취소", description = "PENDING 상태의 예약만 취소할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "취소 성공"),
            @ApiResponse(responseCode = "400", description = "이미 실행됐거나 취소된 예약"),
            @ApiResponse(responseCode = "403", description = "본인 예약이 아닌 경우"),
            @ApiResponse(responseCode = "404", description = "예약 없음")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelSchedule(
            @Parameter(description = "예약 ID") @PathVariable String id) {
        String requesterId = currentUserId();
        log.info("DELETE /api/messages/schedule/{} - requesterId={}", id, requesterId);
        commandService.cancelScheduledMessage(id, requesterId);
        return ResponseEntity.noContent().build();
    }

    // ── helper ────────────────────────────────────────────────────────

    private String currentUserId() {
        return SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
    }
}
