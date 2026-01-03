package com.example.chat.common.core.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ? ì§œ/?œê°„ ? í‹¸ë¦¬í‹°
 */
public final class DateTimeUtil {

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    private DateTimeUtil() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    /**
     * ?„ì¬ ?œê°??Instantë¡?ë°˜í™˜
     */
    public static Instant now() {
        return Instant.now();
    }

    /**
     * ?„ì¬ ?œê°??LocalDateTime?¼ë¡œ ë°˜í™˜ (?œìš¸ ?œê°„)
     */
    public static LocalDateTime nowLocalDateTime() {
        return LocalDateTime.now(DEFAULT_ZONE);
    }

    /**
     * Instantë¥?LocalDateTime?¼ë¡œ ë³€??
     */
    public static LocalDateTime toLocalDateTime(Instant instant) {
        if (instant == null) {
            return null;
        }
        return LocalDateTime.ofInstant(instant, DEFAULT_ZONE);
    }

    /**
     * LocalDateTime??Instantë¡?ë³€??
     */
    public static Instant toInstant(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.atZone(DEFAULT_ZONE).toInstant();
    }

    /**
     * Instantë¥?ISO 8601 ?•ì‹ ë¬¸ì?´ë¡œ ë³€??
     */
    public static String format(Instant instant) {
        if (instant == null) {
            return null;
        }
        return ZonedDateTime.ofInstant(instant, DEFAULT_ZONE).format(ISO_FORMATTER);
    }

    /**
     * ISO 8601 ?•ì‹ ë¬¸ì?´ì„ Instantë¡?ë³€??
     */
    public static Instant parse(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.trim().isEmpty()) {
            return null;
        }
        return ZonedDateTime.parse(dateTimeString, ISO_FORMATTER).toInstant();
    }

    /**
     * ???œê° ?¬ì´??ì´?ì°¨ì´ ê³„ì‚°
     */
    public static long secondsBetween(Instant start, Instant end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Start and end must not be null");
        }
        return end.getEpochSecond() - start.getEpochSecond();
    }
}
