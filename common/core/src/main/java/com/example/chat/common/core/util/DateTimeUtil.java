package com.example.chat.common.core.util;

import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 날짜 변환 유틸리티
 */
@UtilityClass
public final class DateTimeUtil {

	private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Seoul");
	private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

	public static Instant now() {
		return Instant.now();
	}

	public static LocalDateTime nowLocalDateTime() {
		return LocalDateTime.now(DEFAULT_ZONE);
	}

	public static LocalDateTime toLocalDateTime(Instant instant) {
		if (instant == null) {
			return null;
		}
		return LocalDateTime.ofInstant(instant, DEFAULT_ZONE);
	}

	public static Instant toInstant(LocalDateTime localDateTime) {
		if (localDateTime == null) {
			return null;
		}
		return localDateTime.atZone(DEFAULT_ZONE).toInstant();
	}

	public static String format(Instant instant) {
		if (instant == null) {
			return null;
		}
		return ZonedDateTime.ofInstant(instant, DEFAULT_ZONE).format(ISO_FORMATTER);
	}

	public static Instant parse(String dateTimeString) {
		if (dateTimeString == null || dateTimeString.trim().isEmpty()) {
			return null;
		}
		return ZonedDateTime.parse(dateTimeString, ISO_FORMATTER).toInstant();
	}

	public static long secondsBetween(Instant start, Instant end) {
		if (start == null || end == null) {
			throw new IllegalArgumentException("Start and end must not be null");
		}
		return end.getEpochSecond() - start.getEpochSecond();
	}
}
