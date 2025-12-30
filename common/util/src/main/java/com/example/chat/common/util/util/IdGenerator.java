package com.example.chat.common.util.util;

import java.util.UUID;

/**
 * ID 생성 유틸리티
 * 다양한 형태의 ID 생성 지원
 */
public final class IdGenerator {

    private IdGenerator() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    /**
     * UUID 기반 랜덤 ID 생성 (하이픈 포함)
     */
    public static String generateUuid() {
        return UUID.randomUUID().toString();
    }

    /**
     * UUID 기반 랜덤 ID 생성 (하이픈 제거)
     */
    public static String generateCompactUuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 타임스탬프 기반 ID 생성
     * 형식: {timestamp}_{random}
     */
    public static String generateTimestampBasedId() {
        long timestamp = System.currentTimeMillis();
        int random = (int) (Math.random() * 10000);
        return String.format("%d_%04d", timestamp, random);
    }

    /**
     * 특정 prefix를 가진 ID 생성
     */
    public static String generateWithPrefix(String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) {
            throw new IllegalArgumentException("Prefix must not be null or empty");
        }
        return prefix + "_" + generateCompactUuid();
    }

    /**
     * 숫자형 ID 생성 (Snowflake 방식 간소화 버전)
     * 주의: 분산 환경에서는 실제 Snowflake 알고리즘 사용 권장
     */
    public static long generateLongId() {
        long timestamp = System.currentTimeMillis();
        int random = (int) (Math.random() * 1000);
        return (timestamp << 10) | random;
    }
}
