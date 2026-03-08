package com.example.chat.channel.controller;

import com.example.chat.channel.application.dto.request.CreateChannelRequest;
import com.example.chat.channel.application.dto.response.ChannelResponse;
import com.example.chat.channel.application.service.ChannelCommandService;
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

/**
 * 채널 생성/수정/삭제 Command 컨트롤러
 *
 * 책임: HTTP 요청/응답, Validation, Service 위임
 */
@Tag(name = "Channel Command", description = "채팅방 생성/수정/삭제 API")
@RestController
@RequestMapping("/api/v1/channels")
@RequiredArgsConstructor
@Slf4j
public class ChannelCommandController {

    private final ChannelCommandService channelCommandService;

    @Operation(summary = "채널 생성", description = "새 채팅방을 생성합니다. DIRECT 타입은 otherUserId 필수.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "채널 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "사용자 없음")
    })
    @PostMapping
    public ResponseEntity<ChannelResponse> createChannel(@Valid @RequestBody CreateChannelRequest request) {
        log.info("POST /api/v1/channels - type={}", request.type());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(channelCommandService.createChannel(request));
    }

    @Operation(summary = "채널 비활성화", description = "채널 소유자만 채널을 비활성화(소프트 삭제)할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "비활성화 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "채널 없음")
    })
    @DeleteMapping("/{channelId}")
    public ResponseEntity<Void> deactivateChannel(
            @Parameter(description = "채널 ID") @PathVariable String channelId) {
        log.info("DELETE /api/v1/channels/{}", channelId);
        channelCommandService.deactivateChannel(channelId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "채널 멤버 추가", description = "채널 소유자가 멤버를 추가합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "멤버 추가 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "409", description = "이미 멤버")
    })
    @PostMapping("/{channelId}/members/{userId}")
    public ResponseEntity<Void> addMember(
            @PathVariable String channelId,
            @PathVariable String userId) {
        log.info("POST /api/v1/channels/{}/members/{}", channelId, userId);
        channelCommandService.addMember(channelId, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "채널 멤버 제거", description = "소유자 또는 본인이 채널을 떠납니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "멤버 제거 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @DeleteMapping("/{channelId}/members/{userId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable String channelId,
            @PathVariable String userId) {
        log.info("DELETE /api/v1/channels/{}/members/{}", channelId, userId);
        channelCommandService.removeMember(channelId, userId);
        return ResponseEntity.noContent().build();
    }
}
