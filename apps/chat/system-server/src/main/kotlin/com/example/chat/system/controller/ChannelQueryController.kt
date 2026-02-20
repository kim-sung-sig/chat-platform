package com.example.chat.system.controller

import com.example.chat.domain.channel.ChannelType
import com.example.chat.system.application.dto.response.ChannelListItem
import com.example.chat.system.application.query.ChannelListQuery
import com.example.chat.system.application.query.ChannelSortBy
import com.example.chat.system.application.service.ChannelQueryService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/api/channels")
@Tag(name = "Channel Query", description = "채팅방 조회 API")
class ChannelQueryController(
	private val channelQueryService: ChannelQueryService
) {

	@GetMapping
	@Operation(
		summary = "채팅방 목록 조회",
		description = "다양한 필터링과 정렬 옵션을 지원하는 채팅방 목록 조회"
	)
	fun getChannelList(
		@RequestHeader("X-User-Id") userId: String,

		@Parameter(description = "채널 타입 필터 (DIRECT, GROUP, PUBLIC, PRIVATE)")
		@RequestParam(required = false) type: ChannelType?,

		@Parameter(description = "즐겨찾기만 보기")
		@RequestParam(required = false) onlyFavorites: Boolean?,

		@Parameter(description = "읽지 않은 메시지가 있는 것만 보기")
		@RequestParam(required = false) onlyUnread: Boolean?,

		@Parameter(description = "상단 고정만 보기")
		@RequestParam(required = false) onlyPinned: Boolean?,

		@Parameter(description = "검색 키워드 (채널명, 상대방 이름)")
		@RequestParam(required = false) search: String?,

		@Parameter(description = "정렬 기준 (LAST_ACTIVITY, NAME, UNREAD_COUNT, CREATED_AT)")
		@RequestParam(required = false, defaultValue = "LAST_ACTIVITY") sortBy: ChannelSortBy,

		@Parameter(description = "정렬 방향 (ASC, DESC)")
		@RequestParam(required = false, defaultValue = "DESC") direction: ChannelListQuery.SortDirection,

		@Parameter(description = "페이지 번호 (0부터 시작)")
		@RequestParam(required = false, defaultValue = "0") page: Int,

		@Parameter(description = "페이지 크기")
		@RequestParam(required = false, defaultValue = "20") size: Int
	): ResponseEntity<Page<ChannelListItem>> {
		logger.info { "GET /api/channels - userId: $userId, type: $type, favorites: $onlyFavorites, unread: $onlyUnread, search: '$search', page: $page/$size" }

		val query = ChannelListQuery(
			userId = userId,
			type = type,
			onlyFavorites = onlyFavorites,
			onlyUnread = onlyUnread,
			onlyPinned = onlyPinned,
			searchKeyword = search,
			sortBy = sortBy,
			direction = direction,
			page = page,
			size = size
		)

		val result = channelQueryService.getChannelList(query)

		logger.info { "Found ${result.numberOfElements} channels (total: ${result.totalElements})" }

		return ResponseEntity.ok(result)
	}
}
