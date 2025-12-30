package com.example.chat.common.util.constants;

import lombok.experimental.UtilityClass;

/**
 * 시스템 상수
 */
@UtilityClass
public final class SystemConstants {
    // Pagination
    final int DEFAULT_PAGE_SIZE = 50;
    final int MAX_PAGE_SIZE = 100;
    final int MIN_PAGE_SIZE = 10;

    // Message
    final int MAX_MESSAGE_LENGTH = 5000;
    final int MAX_FILE_SIZE_MB = 10;

    // Cache TTL (seconds)
    final long CACHE_TTL_SHORT = 60L; // 1분
    final long CACHE_TTL_MEDIUM = 300L; // 5분
    final long CACHE_TTL_LONG = 3600L; // 1시간
    final long CACHE_TTL_VERY_LONG = 86400L; // 1일

    // Retry
    final int MAX_RETRY_ATTEMPTS = 3;
    final long RETRY_DELAY_MS = 1000L;

    // Thread Pool
    final int CORE_POOL_SIZE = 10;
    final int MAX_POOL_SIZE = 50;
    final int QUEUE_CAPACITY = 100;
}
