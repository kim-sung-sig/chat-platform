package com.example.chat.common.util.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 날짜/시간 유틸리티
 */
public final class DateTimeUtil {

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    private DateTimeUtil() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    /**
     * 현재 시각을 Instant로 반환
     */
    public static Instant now() {
        return Instant.now();
    }

    /**
     * 현재 시각을 LocalDateTime으로 반환 (서울 시간)
     */
    public static LocalDateTime nowLocalDateTime() {
        return LocalDateTime.now(DEFAULT_ZONE);
    }

    /**
     * Instant를 LocalDateTime으로 변환
     */
    public static LocalDateTime toLocalDateTime(Instant instant) {
        if (instant == null) {
            return null;
        }
        return LocalDateTime.ofInstant(instant, DEFAULT_ZONE);
    }

    /**
     * LocalDateTime을 Instant로 변환
     */
    public static Instant toInstant(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.atZone(DEFAULT_ZONE).toInstant();
    }

    /**
     * Instant를 ISO 8601 형식 문자열로 변환
     */
    public static String format(Instant instant) {
        if (instant == null) {
            return null;
        }
        return ZonedDateTime.ofInstant(instant, DEFAULT_ZONE).format(ISO_FORMATTER);
    }

    /**
     * ISO 8601 형식 문자열을 Instant로 변환
     */
    public static Instant parse(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.trim().isEmpty()) {
            return null;
        }
        return ZonedDateTime.parse(dateTimeString, ISO_FORMATTER).toInstant();
    }

    /**
     * 두 시각 사이의 초 차이 계산
     */
    public static long secondsBetween(Instant start, Instant end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Start and end must not be null");
        }
        return end.getEpochSecond() - start.getEpochSecond();
    }
}
