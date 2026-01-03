package com.example.chat.common.core.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Cursor ?∏ÏΩî???îÏΩî???†Ìã∏Î¶¨Ìã∞
 * Cursor Í∞íÏùÑ Base64Î°??∏ÏΩî?©Ìïò???∏Ï∂ú Î∞©Ï?
 */
public final class CursorCodec {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private CursorCodec() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    /**
     * Cursor Í∞íÏùÑ Base64Î°??∏ÏΩî??
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
     * Base64Î°??∏ÏΩî?©Îêú Cursor Í∞íÏùÑ ?îÏΩî??
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
     * Long ?Ä??Cursor ?îÏΩî??(Í∞Ä??ÎßéÏù¥ ?¨Ïö©)
     */
    public static Long decodeLong(String encoded) {
        return decode(encoded, Long.class);
    }

    /**
     * String ?Ä??Cursor ?îÏΩî??
     */
    public static String decodeString(String encoded) {
        return decode(encoded, String.class);
    }
}
