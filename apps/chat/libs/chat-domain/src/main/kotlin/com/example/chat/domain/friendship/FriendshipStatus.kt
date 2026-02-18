package com.example.chat.domain.friendship

/**
 * 친구 관계 상태
 */
enum class FriendshipStatus {
	/**
	 * 대기 중 (친구 요청이 전송되었지만 아직 수락되지 않음)
	 */
	PENDING,

	/**
	 * 수락됨 (친구 관계 성립)
	 */
	ACCEPTED,

	/**
	 * 차단됨 (일방적 차단)
	 */
	BLOCKED
}
