package com.example.chat.file.domain.model;

import java.util.Map;

/**
 * 파일 유형 — MIME 타입 기반 분류
 *
 * 허용된 MIME 타입만 MIME_MAP에 등록되며,
 * 등록되지 않은 MIME는 OTHER로 분류되어 업로드 거부된다.
 */
public enum FileType {

    IMAGE, DOCUMENT, AUDIO, VIDEO, OTHER;

    private static final Map<String, FileType> MIME_MAP = Map.ofEntries(
            Map.entry("image/jpeg",       IMAGE),
            Map.entry("image/png",        IMAGE),
            Map.entry("image/gif",        IMAGE),
            Map.entry("image/webp",       IMAGE),
            Map.entry("application/pdf",  DOCUMENT),
            Map.entry("application/vnd.openxmlformats-officedocument.wordprocessingml.document",   DOCUMENT),
            Map.entry("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",         DOCUMENT),
            Map.entry("application/vnd.openxmlformats-officedocument.presentationml.presentation", DOCUMENT),
            Map.entry("audio/mpeg",       AUDIO),
            Map.entry("audio/aac",        AUDIO),
            Map.entry("video/mp4",        VIDEO),
            Map.entry("video/webm",       VIDEO)
    );

    /**
     * MIME 타입으로 FileType 결정.
     * 허용 목록에 없으면 OTHER 반환.
     */
    public static FileType from(String mimeType) {
        if (mimeType == null) return OTHER;
        return MIME_MAP.getOrDefault(mimeType.toLowerCase(), OTHER);
    }

    /** 업로드 허용 여부 */
    public boolean isAllowed() {
        return this != OTHER;
    }

    public boolean isImage() {
        return this == IMAGE;
    }
}
