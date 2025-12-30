package com.example.chat.common.util.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Cursor 인코딩/디코딩 유틸리티
 * Cursor 값을 Base64로 인코딩하여 노출 방지
 */
public final class CursorCodec {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private CursorCodec() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    /**
     * Cursor 값을 Base64로 인코딩
     */
    public static String encode(Object value) {
        if (value == null) {
            return null;
        }

        try {
            String json = OBJECT_MAPPER.writeValueAsString(value);
            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(json.getBytes(StandardCharsets.UTF_8));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to encode cursor", e);
        }
    }

    /**
     * Base64로 인코딩된 Cursor 값을 디코딩
     */
    public static <T> T decode(String encoded, Class<T> valueType) {
        if (encoded == null || encoded.trim().isEmpty()) {
            return null;
        }

        try {
            byte[] decoded = Base64.getUrlDecoder().decode(encoded);
            String json = new String(decoded, StandardCharsets.UTF_8);
            return OBJECT_MAPPER.readValue(json, valueType);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to decode cursor", e);
        }
    }

    /**
     * Long 타입 Cursor 디코딩 (가장 많이 사용)
     */
    public static Long decodeLong(String encoded) {
        return decode(encoded, Long.class);
    }

    /**
     * String 타입 Cursor 디코딩
     */
    public static String decodeString(String encoded) {
        return decode(encoded, String.class);
    }
}
