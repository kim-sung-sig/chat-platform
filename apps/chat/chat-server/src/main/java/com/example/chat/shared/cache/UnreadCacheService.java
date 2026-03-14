package com.example.chat.shared.cache;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 채팅방 미읽음 수 Redis Hash 캐시 서비스 (Phase 9)
 *
 * 구조:
 *   Key:   chat:channel:{channelId}:unread
 *   Field: {userId}
 *   Value: unreadCount (정수 문자열)
 *   TTL:   24h
 *
 * Write/Read 이중 관리:
 *   메시지 발송 → HINCRBY (발신자 제외, Pipeline 일괄)
 *   읽음 처리  → HSET 0
 *   퇴장 처리  → HDEL
 *
 * Read Model 우선순위:
 *   Redis HGET → hit  : Redis 값 반환
 *             → miss : 호출자가 PostgreSQL fallback 수행
 *
 * 장애 격리:
 *   Redis 예외 → warn 로그 후 Optional.empty() 반환
 *   → PostgreSQL fallback으로 서비스 중단 없이 동작
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UnreadCacheService {

    private static final String KEY_TEMPLATE = "chat:channel:%s:unread";
    /** 24시간 TTL (세션 만료와 동기화) */
    private static final long TTL_SECONDS = 24 * 60 * 60L;

    private final RedisTemplate<String, String> redisTemplate;

    // ─────────────────────────────────────────────
    // Write Operations
    // ─────────────────────────────────────────────

    /**
     * 메시지 발송 시: 발신자를 제외한 모든 멤버 미읽음 수 +1
     * Redis Pipeline으로 N개의 HINCRBY를 단일 네트워크 왕복 처리 (쓰기 최적화)
     *
     * @param channelId 채널 ID
     * @param senderId  발신자 ID (제외 대상)
     * @param memberIds 채널 전체 멤버 ID 목록
     */
    public void incrementAllMembers(String channelId, String senderId, List<String> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) return;

        String key = key(channelId);
        try {
            redisTemplate.executePipelined((RedisCallback<Object>) conn -> {
                byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
                for (String memberId : memberIds) {
                    if (!memberId.equals(senderId)) {
                        conn.hashCommands().hIncrBy(
                                keyBytes,
                                memberId.getBytes(StandardCharsets.UTF_8),
                                1L);
                    }
                }
                // 마지막 접근 기준 TTL 갱신
                conn.keyCommands().expire(keyBytes, TTL_SECONDS);
                return null;
            });
            log.debug("UnreadCache incremented: channelId={}, membersUpdated={}",
                    channelId, memberIds.size() - 1);
        } catch (Exception e) {
            // Redis 장애 시 PostgreSQL 값을 기준으로 계속 동작 (Degraded Mode)
            log.warn("Failed to increment unread cache (degraded mode): channelId={}", channelId, e);
        }
    }

    /**
     * 읽음 처리 시: 해당 사용자의 미읽음 수 → 0 리셋
     *
     * @param channelId 채널 ID
     * @param userId    읽음 처리한 사용자 ID
     */
    public void resetUser(String channelId, String userId) {
        try {
            String key = key(channelId);
            redisTemplate.opsForHash().put(key, userId, "0");
            redisTemplate.expire(key, TTL_SECONDS, TimeUnit.SECONDS);
            log.debug("UnreadCache reset: channelId={}, userId={}", channelId, userId);
        } catch (Exception e) {
            log.warn("Failed to reset unread cache: channelId={}, userId={}", channelId, userId, e);
        }
    }

    /**
     * 멤버 퇴장 시: Redis Hash에서 해당 사용자 필드 제거
     * (MemberLeftKafkaConsumer에서 metadata 삭제와 쌍으로 호출)
     *
     * @param channelId 채널 ID
     * @param userId    퇴장한 사용자 ID
     */
    public void evictUser(String channelId, String userId) {
        try {
            redisTemplate.opsForHash().delete(key(channelId), (Object) userId);
            log.debug("UnreadCache evicted: channelId={}, userId={}", channelId, userId);
        } catch (Exception e) {
            log.warn("Failed to evict unread cache: channelId={}, userId={}", channelId, userId, e);
        }
    }

    // ─────────────────────────────────────────────
    // Read Operations
    // ─────────────────────────────────────────────

    /**
     * 채널 목록 조회 시: 미읽음 수 조회 (Redis-first)
     * - hit:  Integer 반환
     * - miss: Optional.empty() → 호출자가 PostgreSQL metadata.unreadCount 사용
     *
     * @param channelId 채널 ID
     * @param userId    조회 대상 사용자 ID
     * @return 캐시된 미읽음 수, 없으면 empty
     */
    public Optional<Integer> getUnreadCount(String channelId, String userId) {
        try {
            Object val = redisTemplate.opsForHash().get(key(channelId), userId);
            if (val == null) return Optional.empty();
            return Optional.of(Integer.parseInt(val.toString()));
        } catch (Exception e) {
            log.warn("Failed to get unread cache: channelId={}, userId={}", channelId, userId, e);
            return Optional.empty();
        }
    }

    // ─────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────

    private String key(String channelId) {
        return String.format(KEY_TEMPLATE, channelId);
    }
}
