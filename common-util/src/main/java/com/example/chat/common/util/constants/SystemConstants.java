package com.example.chat.common.util.constants;

/**
 * 시스템 상수
 */
public final class SystemConstants {

    private SystemConstants() {
        throw new AssertionError("Cannot instantiate constants class");
    }

    // Pagination
    public static final int DEFAULT_PAGE_SIZE = 50;
    public static final int MAX_PAGE_SIZE = 100;
    public static final int MIN_PAGE_SIZE = 10;

    // Message
    public static final int MAX_MESSAGE_LENGTH = 5000;
    public static final int MAX_FILE_SIZE_MB = 10;

    // Cache TTL (seconds)
    public static final long CACHE_TTL_SHORT = 60L;        // 1분
    public static final long CACHE_TTL_MEDIUM = 300L;      // 5분
    public static final long CACHE_TTL_LONG = 3600L;       // 1시간
    public static final long CACHE_TTL_VERY_LONG = 86400L; // 1일

    // Retry
    public static final int MAX_RETRY_ATTEMPTS = 3;
    public static final long RETRY_DELAY_MS = 1000L;

    // Thread Pool
    public static final int CORE_POOL_SIZE = 10;
    public static final int MAX_POOL_SIZE = 50;
    public static final int QUEUE_CAPACITY = 100;
}
