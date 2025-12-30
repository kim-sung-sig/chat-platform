package com.example.chat.websocket.domain.session;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 로컬 세션 관리자
 * 현재 인스턴스의 WebSocket 세션만 관리 (단일 책임)
 */
@Slf4j
@Component
public class LocalSessionManager implements SessionManager {

	private final Map<String, ChatSession> sessionById = new ConcurrentHashMap<>();
	private final Map<String, Set<ChatSession>> sessionsByRoom = new ConcurrentHashMap<>();

	@Override
	public void register(ChatSession session) {
		if (!validateSession(session)) {
			return;
		}

		if (isDuplicate(session)) {
			return;
		}

		addToMaps(session);

		log.info("Local session registered: sessionId={}, roomId={}",
				session.getSessionId(), session.getRoomId());
	}

	@Override
	public void remove(String sessionId) {
		if (sessionId == null) {
			log.warn("Cannot remove null sessionId");
			return;
		}

		ChatSession session = sessionById.remove(sessionId);

		if (session == null) {
			return;
		}

		removeFromMaps(session);

		log.info("Local session removed: sessionId={}", sessionId);
	}

	@Override
	public Optional<ChatSession> findById(String sessionId) {
		return Optional.ofNullable(sessionById.get(sessionId));
	}

	@Override
	public List<ChatSession> findActiveByRoom(String roomId) {
		if (roomId == null) {
			return Collections.emptyList();
		}

		Set<ChatSession> sessions = sessionsByRoom.get(roomId);

		if (sessions == null) {
			return Collections.emptyList();
		}

		return sessions.stream()
				.filter(ChatSession::isActive)
				.collect(Collectors.toList());
	}

	@Override
	public List<ChatSession> findActiveByUser(Long userId) {
		if (userId == null) {
			return Collections.emptyList();
		}

		return sessionById.values().stream()
				.filter(session -> userId.equals(session.getUserId()))
				.filter(ChatSession::isActive)
				.collect(Collectors.toList());
	}

	/**
	 * 전체 활성 세션 수
	 */
	public int countActive() {
		return (int) sessionById.values().stream()
				.filter(ChatSession::isActive)
				.count();
	}

	// ========== Private Methods ==========

	private boolean validateSession(ChatSession session) {
		if (session == null || session.getSessionId() == null) {
			log.warn("Invalid session");
			return false;
		}
		return true;
	}

	private boolean isDuplicate(ChatSession session) {
		if (sessionById.containsKey(session.getSessionId())) {
			log.warn("Session already registered: {}", session.getSessionId());
			return true;
		}
		return false;
	}

	private void addToMaps(ChatSession session) {
		sessionById.put(session.getSessionId(), session);

		sessionsByRoom
				.computeIfAbsent(session.getRoomId(), k -> ConcurrentHashMap.newKeySet())
				.add(session);
	}

	private void removeFromMaps(ChatSession session) {
		Set<ChatSession> sessions = sessionsByRoom.get(session.getRoomId());

		if (sessions != null) {
			sessions.remove(session);

			if (sessions.isEmpty()) {
				sessionsByRoom.remove(session.getRoomId());
			}
		}
	}
}
