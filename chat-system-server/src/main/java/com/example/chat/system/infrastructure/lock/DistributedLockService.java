package com.example.chat.system.infrastructure.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Redis 기반 분산 락 서비스
 * 멀티 인스턴스 환경에서 동시 실행 방지
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DistributedLockService {

    private static final String LOCK_KEY_PREFIX = "lock:schedule:";
    private static final Duration DEFAULT_TIMEOUT = Duration.ofMinutes(5);

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 락 획득 시도
     */
    public boolean tryLock(Long scheduleId) {
        return tryLock(scheduleId, DEFAULT_TIMEOUT);
    }

    /**
     * 락 획득 시도 (타임아웃 지정)
     */
    public boolean tryLock(Long scheduleId, Duration timeout) {
        // Early return: null 체크
        if (scheduleId == null) {
            log.warn("Cannot acquire lock: scheduleId is null");
            return false;
        }

        String key = LOCK_KEY_PREFIX + scheduleId;
        String value = Thread.currentThread().getName();

        try {
            Boolean result = redisTemplate.opsForValue()
                    .setIfAbsent(key, value, timeout);

            if (Boolean.TRUE.equals(result)) {
                log.debug("Lock acquired: scheduleId={}, thread={}",
                    scheduleId, value);
                return true;
            } else {
                log.debug("Lock already held: scheduleId={}", scheduleId);
                return false;
            }

        } catch (Exception e) {
            log.error("Failed to acquire lock: scheduleId={}", scheduleId, e);
            return false;
        }
    }

    /**
     * 락 해제
     */
    public void unlock(Long scheduleId) {
        // Early return: null 체크
        if (scheduleId == null) {
            log.warn("Cannot release lock: scheduleId is null");
            return;
        }

        String key = LOCK_KEY_PREFIX + scheduleId;

        try {
            redisTemplate.delete(key);
            log.debug("Lock released: scheduleId={}", scheduleId);

        } catch (Exception e) {
            log.error("Failed to release lock: scheduleId={}", scheduleId, e);
        }
    }

    /**
     * 락 강제 해제 (긴급 상황용)
     */
    public void forceUnlock(Long scheduleId) {
        unlock(scheduleId);
        log.warn("Lock forcefully released: scheduleId={}", scheduleId);
    }
}
