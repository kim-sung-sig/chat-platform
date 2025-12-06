package com.example.chat.websocket.domain.session;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 통합 세션 관리자 (Facade 패턴)
 * 로컬 세션 + Redis 메타데이터를 통합 관리
 *
 * 책임:
 * - 로컬 세션 관리 위임 (LocalSessionManager)
 * - Redis 동기화 (RedisSessionMetadataManager)
 * - 두 관리자의 일관성 보장
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatRoomSessionManager {

    private final LocalSessionManager localSessionManager;
    private final RedisSessionMetadataManager redisSessionMetadataManager;

    /**
     * 세션 등록 (로컬 + Redis 동기화)
     */
    public void registerSession(ChatSession session) {
        // Step 1: 로컬 등록
        localSessionManager.register(session);

        // Step 2: Redis 동기화
        redisSessionMetadataManager.registerSessionMetadata(
            session.getSessionId(),
            session.getUserId(),
            session.getRoomId()
        );
    }

    /**
     * 세션 제거 (로컬 + Redis 동기화)
     */
    public void removeSession(String sessionId) {
        // Step 1: 로컬 제거
        localSessionManager.remove(sessionId);

        // Step 2: Redis 동기화
        redisSessionMetadataManager.removeSessionMetadata(sessionId);
    }

    /**
     * 세션 조회 (로컬)
     */
    public Optional<ChatSession> getSession(String sessionId) {
        return localSessionManager.findById(sessionId);
    }

    /**
     * 채팅방의 활성 세션 조회 (로컬만)
     */
    public List<ChatSession> getActiveSessionsByRoom(String roomId) {
        return localSessionManager.findActiveByRoom(roomId);
    }

    /**
     * 사용자의 활성 세션 조회 (로컬만)
     */
    public List<ChatSession> getActiveSessionsByUser(Long userId) {
        return localSessionManager.findActiveByUser(userId);
    }

    /**
     * 로컬 활성 세션 수
     */
    public int getActiveSessionCount(String roomId) {
        return getActiveSessionsByRoom(roomId).size();
    }

    /**
     * 전체 세션 수 (모든 인스턴스, Redis)
     */
    public long getTotalSessionCountByRoom(String roomId) {
        return redisSessionMetadataManager.getSessionCountByRoom(roomId);
    }

    /**
     * 로컬 전체 활성 세션 수
     */
    public int getTotalActiveSessionCount() {
        return localSessionManager.countActive();
    }
}
