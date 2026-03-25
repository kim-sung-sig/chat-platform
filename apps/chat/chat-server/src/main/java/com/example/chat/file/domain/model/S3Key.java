package com.example.chat.file.domain.model;

import java.util.UUID;

/**
 * S3 오브젝트 키 — Value Object
 *
 * 형식: {channelId}/{uuid}/{normalizedFileName}
 */
public record S3Key(String value) {

    public S3Key {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("S3Key must not be blank");
        }
        String[] parts = value.split("/");
        if (parts.length < 3) {
            throw new IllegalArgumentException("Invalid S3Key format: " + value);
        }
    }

    /**
     * channelId와 원본 파일명으로 새 S3Key 생성.
     * 파일명 내 특수문자는 '_'로 치환한다.
     */
    public static S3Key of(String channelId, String originalFileName) {
        String normalized = originalFileName.replaceAll("[^a-zA-Z0-9._-]", "_");
        return new S3Key(channelId + "/" + UUID.randomUUID() + "/" + normalized);
    }
}
