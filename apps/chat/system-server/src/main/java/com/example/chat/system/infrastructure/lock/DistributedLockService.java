package com.example.chat.system.infrastructure.lock;

import java.time.Duration;
import java.util.Objects;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Redis 기반 분산 락 서비스
 * 멀티 인스턴스 환경에서 동시 실행 방지
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DistributedLockService {

	private static final String LOCK_KEY_PREFIX = "lock:schedule:";
	private static final @NonNull Duration DEFAULT_TIMEOUT = Objects.requireNonNull(Duration.ofMinutes(5));

	private final RedisTemplate<String, String> redisTemplate;

	/**
	 * 락 획득 시도
	 */
	public boolean tryLock(@NonNull String scheduleId) {
		return tryLock(scheduleId, DEFAULT_TIMEOUT);
	}

	/**
	 * 락 획득 시도 (타임아웃 지정)
	 */
	public boolean tryLock(@NonNull String scheduleId, @NonNull Duration timeout) {

		String key = LOCK_KEY_PREFIX + scheduleId;
		String value = Objects.requireNonNull(Thread.currentThread().getName());

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
	public void unlock(String scheduleId) {
		// Early return: null 체크
		if (scheduleId == null || scheduleId.isBlank()) {
			log.warn("Cannot release lock: scheduleId is null or blank");
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
	public void forceUnlock(String scheduleId) {
		unlock(scheduleId);
		log.warn("Lock forcefully released: scheduleId={}", scheduleId);
	}
}
