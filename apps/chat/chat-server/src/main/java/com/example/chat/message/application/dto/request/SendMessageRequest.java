package com.example.chat.message.application.dto.request;

import java.util.Map;

import com.example.chat.message.domain.MessageContent;
import com.example.chat.common.core.enums.MessageType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 메시지 발송 요청 DTO
 *
 * - toMessageContent() 를 통해 타입별 분기를 DTO 내부에 캡슐화
 * - Application Service 에서 switch 분기 제거 → OCP 준수
 */
public record SendMessageRequest(
        @NotBlank(message = "channelId is required") String channelId,
        @NotNull(message = "messageType is required") MessageType messageType,
        @NotNull(message = "payload is required") Map<String, Object> payload) {

    /**
     * Payload → MessageContent 변환 (타입별 분기 캡슐화)
     * 새로운 MessageType 추가 시 이 메서드만 수정한다.
     */
    public MessageContent toMessageContent() {
        return switch (messageType) {
            case TEXT -> MessageContent.text(require("text"));
            case IMAGE -> MessageContent.image(
                    require("imageUrl"),
                    orDefault("fileName", "image.jpg"),
                    longOrDefault("fileSize", 0L));
            case FILE -> MessageContent.file(
                    require("fileUrl"),
                    require("fileName"),
                    longOrDefault("fileSize", 0L),
                    orDefault("mimeType", "application/octet-stream"));
            case VIDEO -> MessageContent.file(
                    require("videoUrl"),
                    orDefault("fileName", "video.mp4"),
                    longOrDefault("fileSize", 0L),
                    orDefault("mimeType", "video/mp4"));
            case AUDIO -> MessageContent.file(
                    require("audioUrl"),
                    orDefault("fileName", "audio.mp3"),
                    longOrDefault("fileSize", 0L),
                    orDefault("mimeType", "audio/mpeg"));
            case SYSTEM -> MessageContent.text(require("text"));
        };
    }

    private String require(String field) {
        Object value = payload.get(field);
        if (value == null || value.toString().isBlank()) {
            throw new IllegalArgumentException("Field '" + field + "' is required in payload for type " + messageType);
        }
        return value.toString();
    }

    private String orDefault(String field, String defaultValue) {
        Object value = payload.get(field);
        return (value != null && !value.toString().isBlank()) ? value.toString() : defaultValue;
    }

    private long longOrDefault(String field, long defaultValue) {
        Object value = payload.get(field);
        if (value == null) return defaultValue;
        if (value instanceof Number n) return n.longValue();
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
