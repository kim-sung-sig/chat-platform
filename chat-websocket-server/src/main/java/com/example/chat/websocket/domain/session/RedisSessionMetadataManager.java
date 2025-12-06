package com.example.chat.websocket.domain.session;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Set;

/**
 * Redis 기반 세션 메타데이터 관리자
 * 멀티 인스턴스 환경에서 세션 정보를 공유
 *
 * 실제 WebSocketSession은 각 인스턴스에만 존재하지만,
 * 세션 메타데이터(userId, roomId)는 Redis에 저장하여 공유
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisSessionMetadataManager {

    private static final String ROOM_SESSIONS_KEY_PREFIX = "chat:room:sessions:";
    private static final String USER_SESSIONS_KEY_PREFIX = "chat:user:sessions:";
    private static final String SESSION_INFO_KEY_PREFIX = "chat:session:info:";
    private static final Duration SESSION_TTL = Duration.ofHours(24);

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 세션 메타데이터 등록
     */
    public void registerSessionMetadata(String sessionId, Long userId, String roomId) {
        // Early return: null 체크
        if (sessionId == null || roomId == null) {
            log.warn("Cannot register session metadata: sessionId or roomId is null");
            return;
        }

        try {
            // Step 1: 세션 정보 저장 (sessionId -> userId:roomId)
            String sessionInfoKey = SESSION_INFO_KEY_PREFIX + sessionId;
            String sessionInfo = userId + ":" + roomId;
            redisTemplate.opsForValue().set(sessionInfoKey, sessionInfo, SESSION_TTL);

            // Step 2: 채팅방별 세션 목록에 추가
            String roomSessionsKey = ROOM_SESSIONS_KEY_PREFIX + roomId;
            redisTemplate.opsForSet().add(roomSessionsKey, sessionId);
            redisTemplate.expire(roomSessionsKey, SESSION_TTL);

            // Step 3: 사용자별 세션 목록에 추가 (userId가 있는 경우)
            if (userId != null) {
                String userSessionsKey = USER_SESSIONS_KEY_PREFIX + userId;
                redisTemplate.opsForSet().add(userSessionsKey, sessionId);
                redisTemplate.expire(userSessionsKey, SESSION_TTL);
            }

            log.info("Session metadata registered in Redis: sessionId={}, userId={}, roomId={}",
                sessionId, userId, roomId);

        } catch (Exception e) {
            log.error("Failed to register session metadata in Redis", e);
        }
    }

    /**
     * 세션 메타데이터 제거
     */
    public void removeSessionMetadata(String sessionId) {
        // Early return: null 체크
        if (sessionId == null) {
            log.warn("Cannot remove session metadata: sessionId is null");
            return;
        }

        try {
            // Step 1: 세션 정보 조회
            String sessionInfoKey = SESSION_INFO_KEY_PREFIX + sessionId;
            String sessionInfo = redisTemplate.opsForValue().get(sessionInfoKey);

            // Early return: 세션 정보가 없으면 종료
            if (sessionInfo == null) {
                log.debug("Session metadata not found in Redis: {}", sessionId);
                return;
            }

            // Step 2: userId와 roomId 파싱
            String[] parts = sessionInfo.split(":");
            String userIdStr = parts.length > 0 ? parts[0] : null;
            String roomId = parts.length > 1 ? parts[1] : null;

            // Step 3: 세션 정보 삭제
            redisTemplate.delete(sessionInfoKey);

            // Step 4: 채팅방별 세션 목록에서 제거
            if (roomId != null) {
                String roomSessionsKey = ROOM_SESSIONS_KEY_PREFIX + roomId;
                redisTemplate.opsForSet().remove(roomSessionsKey, sessionId);
            }

            // Step 5: 사용자별 세션 목록에서 제거
            if (userIdStr != null && !userIdStr.equals("null")) {
                String userSessionsKey = USER_SESSIONS_KEY_PREFIX + userIdStr;
                redisTemplate.opsForSet().remove(userSessionsKey, sessionId);
            }

            log.info("Session metadata removed from Redis: sessionId={}, userId={}, roomId={}",
                sessionId, userIdStr, roomId);

        } catch (Exception e) {
            log.error("Failed to remove session metadata from Redis", e);
        }
    }

    /**
     * 채팅방의 세션 ID 목록 조회 (모든 인스턴스의 세션 포함)
     */
    public Set<String> getSessionIdsByRoom(String roomId) {
        // Early return: null 체크
        if (roomId == null) {
            return Set.of();
        }

        try {
            String roomSessionsKey = ROOM_SESSIONS_KEY_PREFIX + roomId;
            Set<String> sessionIds = redisTemplate.opsForSet().members(roomSessionsKey);

            return sessionIds != null ? sessionIds : Set.of();

        } catch (Exception e) {
            log.error("Failed to get session IDs from Redis for room: {}", roomId, e);
            return Set.of();
        }
    }

    /**
     * 사용자의 세션 ID 목록 조회 (모든 인스턴스의 세션 포함)
     */
    public Set<String> getSessionIdsByUser(Long userId) {
        // Early return: null 체크
        if (userId == null) {
            return Set.of();
        }

        try {
            String userSessionsKey = USER_SESSIONS_KEY_PREFIX + userId;
            Set<String> sessionIds = redisTemplate.opsForSet().members(userSessionsKey);

            return sessionIds != null ? sessionIds : Set.of();

        } catch (Exception e) {
            log.error("Failed to get session IDs from Redis for user: {}", userId, e);
            return Set.of();
        }
    }

    /**
     * 채팅방의 활성 세션 수 (모든 인스턴스 포함)
     */
    public long getSessionCountByRoom(String roomId) {
        // Early return: null 체크
        if (roomId == null) {
            return 0;
        }

        try {
            String roomSessionsKey = ROOM_SESSIONS_KEY_PREFIX + roomId;
            Long size = redisTemplate.opsForSet().size(roomSessionsKey);

            return size != null ? size : 0;

        } catch (Exception e) {
            log.error("Failed to get session count from Redis for room: {}", roomId, e);
            return 0;
        }
    }
}
