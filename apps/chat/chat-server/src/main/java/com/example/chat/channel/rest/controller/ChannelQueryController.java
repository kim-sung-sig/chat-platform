package com.example.chat.channel.rest.controller;

import java.time.Instant;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.chat.auth.core.util.SecurityUtils;
import com.example.chat.channel.rest.dto.response.ChannelListItem;
import com.example.chat.channel.application.query.ChannelListQuery;
import com.example.chat.channel.application.service.ChannelListQueryService;
import com.example.chat.channel.application.model.CursorPage;
import com.example.chat.common.core.enums.ChannelType;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 채팅방 조회 컨트롤러
 *
 * 책임:
 * - HTTP 요청 수신 및 응답
 * - Validation 처리 (@Valid)
 * - Application Service 위임
 *
 * 커서 페이징:
 * - cursor 파라미터: 이전 응답의 nextCursor 값 (Instant ISO-8601)
 * - null 이면 첫 페이지
 */
@RestController
@RequestMapping("/api/v1/channels")
@Tag(name = "Channel Query", description = "채팅방 조회 API")
@RequiredArgsConstructor
@Slf4j
public class ChannelQueryController {

    private final ChannelListQueryService channelQueryService;

    /**
     * 내 채팅방 목록 조회 (JWT 토큰 기반)
     * GET /api/v1/channels/my
     */
    @GetMapping("/my")
    @Operation(
            summary = "내 채팅방 목록 조회",
            description = "JWT 토큰으로 인증된 사용자의 채팅방 목록 조회 (커서 페이징)"
    )
    public ResponseEntity<CursorPage<ChannelListItem>> getMyChannelList(
            @Parameter(description = "채널 타입 필터 (DIRECT, GROUP, PUBLIC, PRIVATE)")
            @RequestParam(required = false) ChannelType type,

            @Parameter(description = "즐겨찾기만 보기")
            @RequestParam(required = false) Boolean onlyFavorites,

            @Parameter(description = "읽지 않은 메시지가 있는 것만 보기")
            @RequestParam(required = false) Boolean onlyUnread,

            @Parameter(description = "상단 고정만 보기")
            @RequestParam(required = false) Boolean onlyPinned,

            @Parameter(description = "검색 키워드 (채널명, 상대방 이름)")
            @RequestParam(required = false) String search,

            @Parameter(description = "커서 (이전 페이지 마지막 채널의 createdAt ISO-8601, null 이면 첫 페이지)")
            @RequestParam(required = false) Instant cursor,

            @Parameter(description = "페이지 크기")
            @RequestParam(required = false, defaultValue = "20") int size) {

        String userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        log.info("GET /api/v1/channels/my - userId: {}, type: {}, cursor: {}, size: {}", userId, type, cursor, size);

        ChannelListQuery query = new ChannelListQuery(
                userId, type, onlyFavorites, onlyUnread, onlyPinned, search, cursor, size);

        CursorPage<ChannelListItem> result = channelQueryService.getChannelList(query);

        log.info("Found {} channels for user {}", result.content().size(), userId);

        return ResponseEntity.ok(result);
    }

    /**
     * 채팅방 목록 조회 (X-User-Id 헤더 기반, 내부 서비스 간 호출용)
     * GET /api/v1/channels
     */
    @GetMapping
    @Operation(
            summary = "채팅방 목록 조회 (헤더 기반)",
            description = "X-User-Id 헤더로 사용자를 지정하여 채팅방 목록 조회 (커서 페이징)"
    )
    public ResponseEntity<CursorPage<ChannelListItem>> getChannelList(
            @RequestHeader("X-User-Id") String userId,

            @Parameter(description = "채널 타입 필터 (DIRECT, GROUP, PUBLIC, PRIVATE)")
            @RequestParam(required = false) ChannelType type,

            @Parameter(description = "즐겨찾기만 보기")
            @RequestParam(required = false) Boolean onlyFavorites,

            @Parameter(description = "읽지 않은 메시지가 있는 것만 보기")
            @RequestParam(required = false) Boolean onlyUnread,

            @Parameter(description = "상단 고정만 보기")
            @RequestParam(required = false) Boolean onlyPinned,

            @Parameter(description = "검색 키워드 (채널명, 상대방 이름)")
            @RequestParam(required = false) String search,

            @Parameter(description = "커서 (이전 페이지 마지막 채널의 createdAt ISO-8601, null 이면 첫 페이지)")
            @RequestParam(required = false) Instant cursor,

            @Parameter(description = "페이지 크기")
            @RequestParam(required = false, defaultValue = "20") int size) {

        log.info("GET /api/v1/channels - userId: {}, type: {}, cursor: {}, size: {}", userId, type, cursor, size);

        ChannelListQuery query = new ChannelListQuery(
                userId, type, onlyFavorites, onlyUnread, onlyPinned, search, cursor, size);

        CursorPage<ChannelListItem> result = channelQueryService.getChannelList(query);

        log.info("Found {} channels", result.content().size());

        return ResponseEntity.ok(result);
    }
}
