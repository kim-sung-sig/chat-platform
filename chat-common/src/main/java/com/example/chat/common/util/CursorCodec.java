package com.example.chat.common.util;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Cursor encoding/decoding helper.
 * Cursor is encoded as Base64 of: "{createdAtIso}|{id}" where createdAtIso is ISO_OFFSET_DATE_TIME.
 */
public final class CursorCodec {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private CursorCodec() {}

    public static String encode(Cursor cursor) {
        if (cursor == null) return null;
        String raw = (cursor.getCreatedAt() == null ? "" : FORMATTER.format(cursor.getCreatedAt())) + "|" + (cursor.getId() == null ? "" : cursor.getId().toString());
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    public static Cursor decode(String encoded) {
        if (encoded == null || encoded.isBlank()) return null;
        try {
            String raw = new String(Base64.getUrlDecoder().decode(encoded), StandardCharsets.UTF_8);
            String[] parts = raw.split("\\|", 2);
            OffsetDateTime createdAt = parts.length > 0 && !parts[0].isBlank() ? OffsetDateTime.parse(parts[0], FORMATTER) : null;
            Long id = null;
            if (parts.length > 1 && !parts[1].isBlank()) {
                id = Long.parseLong(parts[1]);
            }
            return new Cursor(createdAt, id);
        } catch (Exception e) {
            return null; // invalid cursor -> treated as null
        }
    }
}