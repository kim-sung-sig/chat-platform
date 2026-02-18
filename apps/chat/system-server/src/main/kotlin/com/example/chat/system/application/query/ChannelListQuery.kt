package com.example.chat.system.application.query

import com.example.chat.domain.channel.ChannelType

/**
 * 채팅방 목록 조회 Query 모델
 *
 * CQRS Query Side:
 * - 복잡한 필터링/정렬 지원
 * - 페이징 지원
 */
data class ChannelListQuery(
	val userId: String,

	// 필터 조건
	val type: ChannelType? = null,           // 채널 타입 필터
	val onlyFavorites: Boolean? = null,      // 즐겨찾기만 보기
	val onlyUnread: Boolean? = null,         // 읽지 않은 메시지가 있는 것만
	val onlyPinned: Boolean? = null,         // 상단 고정만 보기
	val searchKeyword: String? = null,       // 채널명 검색

	// 정렬 조건
	val sortBy: ChannelSortBy = ChannelSortBy.LAST_ACTIVITY,
	val direction: SortDirection = SortDirection.DESC,

	// 페이징
	val page: Int = 0,
	val size: Int = 20
) {
	/**
	 * 정렬 방향
	 */
	enum class SortDirection {
		ASC,    // 오름차순
		DESC    // 내림차순
	}
}
