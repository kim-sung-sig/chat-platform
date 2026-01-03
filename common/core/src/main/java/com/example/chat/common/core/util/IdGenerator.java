package com.example.chat.common.core.util;

import lombok.experimental.UtilityClass;

import java.util.UUID;

@UtilityClass
public final class IdGenerator {

    public static String generateUuid() {
        return UUID.randomUUID().toString();
    }

    public static String generateCompactUuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String generateTimestampBasedId() {
        long timestamp = System.currentTimeMillis();
        int random = (int) (Math.random() * 10000);
        return String.format("%d_%04d", timestamp, random);
    }

    public static String generateWithPrefix(String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) {
            throw new IllegalArgumentException("Prefix must not be null or empty");
        }
        return prefix + "_" + generateCompactUuid();
    }

    public static long generateLongId() {
        long timestamp = System.currentTimeMillis();
        int random = (int) (Math.random() * 1000);
        return (timestamp << 10) | random;
    }
}
