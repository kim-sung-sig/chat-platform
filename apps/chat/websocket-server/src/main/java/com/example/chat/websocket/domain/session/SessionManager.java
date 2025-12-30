package com.example.chat.websocket.domain.session;

import java.util.List;
import java.util.Optional;

/**
 * 세션 관리자 인터페이스
 * SRP (Single Responsibility Principle) 적용
 */
public interface SessionManager {

	/**
	 * 세션 등록
	 */
	void register(ChatSession session);

	/**
	 * 세션 제거
	 */
	void remove(String sessionId);

	/**
	 * 세션 조회
	 */
	Optional<ChatSession> findById(String sessionId);

	/**
	 * 채팅방의 활성 세션 조회
	 */
	List<ChatSession> findActiveByRoom(String roomId);

	/**
	 * 사용자의 활성 세션 조회
	 */
	List<ChatSession> findActiveByUser(Long userId);
}
