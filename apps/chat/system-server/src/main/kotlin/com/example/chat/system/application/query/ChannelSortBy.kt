package com.example.chat.system.application.query

/**
 * 채팅방 정렬 기준
 */
enum class ChannelSortBy {
	/**
	 * 마지막 활동 시간 순 (기본값)
	 */
	LAST_ACTIVITY,

	/**
	 * 채널명 순
	 */
	NAME,

	/**
	 * 읽지 않은 메시지 수 순
	 */
	UNREAD_COUNT,

	/**
	 * 생성 시간 순
	 */
	CREATED_AT
}
