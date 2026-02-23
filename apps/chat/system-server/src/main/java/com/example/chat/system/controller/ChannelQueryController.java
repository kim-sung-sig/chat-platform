package com.example.chat.system.controller;

import com.example.chat.domain.channel.ChannelType;
import com.example.chat.system.application.dto.response.ChannelListItem;
import com.example.chat.system.application.query.ChannelListQuery;
import com.example.chat.system.application.query.ChannelSortBy;
import com.example.chat.system.application.service.ChannelQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 채팅방 조회 컨트롤러
 *
 * 책임:
 * - HTTP 요청 수신 및 응답
 * - Validation 처리 (@Valid)
 * - Application Service 위임
 */
@RestController
@RequestMapping("/api/channels")
@Tag(name = "Channel Query", description = "채팅방 조회 API")
@RequiredArgsConstructor
@Slf4j
public class ChannelQueryController {

    private final ChannelQueryService channelQueryService;

    @GetMapping
    @Operation(
            summary = "채팅방 목록 조회",
            description = "다양한 필터링과 정렬 옵션을 지원하는 채팅방 목록 조회"
    )
    public ResponseEntity<Page<ChannelListItem>> getChannelList(
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

            @Parameter(description = "정렬 기준 (LAST_ACTIVITY, NAME, UNREAD_COUNT, CREATED_AT)")
            @RequestParam(required = false, defaultValue = "LAST_ACTIVITY") ChannelSortBy sortBy,

            @Parameter(description = "정렬 방향 (ASC, DESC)")
            @RequestParam(required = false, defaultValue = "DESC") ChannelListQuery.SortDirection direction,

            @Parameter(description = "페이지 번호 (0부터 시작)")
            @RequestParam(required = false, defaultValue = "0") int page,

            @Parameter(description = "페이지 크기")
            @RequestParam(required = false, defaultValue = "20") int size) {

        log.info("GET /api/channels - userId: {}, type: {}, favorites: {}, unread: {}, search: '{}', page: {}/{}",
                userId, type, onlyFavorites, onlyUnread, search, page, size);

        ChannelListQuery query = new ChannelListQuery(
                userId, type, onlyFavorites, onlyUnread, onlyPinned, search, sortBy, direction, page, size);

        Page<ChannelListItem> result = channelQueryService.getChannelList(query);

        log.info("Found {} channels (total: {})", result.getNumberOfElements(), result.getTotalElements());

        return ResponseEntity.ok(result);
    }
}
