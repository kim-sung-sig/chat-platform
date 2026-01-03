package com.example.chat.common.core.util;

import java.util.UUID;

/**
 * ID ?ì„± ? í‹¸ë¦¬í‹°
 * ?¤ì–‘???•íƒœ??ID ?ì„± ì§€??
 */
public final class IdGenerator {

    private IdGenerator() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    /**
     * UUID ê¸°ë°˜ ?œë¤ ID ?ì„± (?˜ì´???¬í•¨)
     */
    public static String generateUuid() {
        return UUID.randomUUID().toString();
    }

    /**
     * UUID ê¸°ë°˜ ?œë¤ ID ?ì„± (?˜ì´???œê±°)
     */
    public static String generateCompactUuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * ?€?„ìŠ¤?¬í”„ ê¸°ë°˜ ID ?ì„±
     * ?•ì‹: {timestamp}_{random}
     */
    public static String generateTimestampBasedId() {
        long timestamp = System.currentTimeMillis();
        int random = (int) (Math.random() * 10000);
        return String.format("%d_%04d", timestamp, random);
    }

    /**
     * ?¹ì • prefixë¥?ê°€ì§?ID ?ì„±
     */
    public static String generateWithPrefix(String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) {
            throw new IllegalArgumentException("Prefix must not be null or empty");
        }
        return prefix + "_" + generateCompactUuid();
    }

    /**
     * ?«ì??ID ?ì„± (Snowflake ë°©ì‹ ê°„ì†Œ??ë²„ì „)
     * ì£¼ì˜: ë¶„ì‚° ?˜ê²½?ì„œ???¤ì œ Snowflake ?Œê³ ë¦¬ì¦˜ ?¬ìš© ê¶Œì¥
     */
    public static long generateLongId() {
        long timestamp = System.currentTimeMillis();
        int random = (int) (Math.random() * 1000);
        return (timestamp << 10) | random;
    }
}
