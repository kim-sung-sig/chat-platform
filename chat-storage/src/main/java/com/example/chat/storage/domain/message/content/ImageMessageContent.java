package com.example.chat.storage.domain.message.content;

import com.example.chat.storage.domain.message.MessageContent;
import com.example.chat.storage.domain.message.MessageType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 이미지 메시지 콘텐츠
 */
@Getter
@Builder
public class ImageMessageContent implements MessageContent {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    private final String imageUrl;
    private final String thumbnailUrl;
    private final Long fileSize;
    private final Integer width;
    private final Integer height;
    private final String mimeType;
    private final String caption; // 선택적 캡션

    @Override
    public MessageType getType() {
        return MessageType.IMAGE;
    }

    @Override
    public String toJson() {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("imageUrl", imageUrl);
            data.put("thumbnailUrl", thumbnailUrl);
            data.put("fileSize", fileSize);
            data.put("width", width);
            data.put("height", height);
            data.put("mimeType", mimeType);
            data.put("caption", caption);
            return OBJECT_MAPPER.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize image message content", e);
        }
    }

    @Override
    public void validate() {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Image URL cannot be empty");
        }

        if (fileSize != null && fileSize > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                String.format("Image file size exceeds max: %d > %d", fileSize, MAX_FILE_SIZE)
            );
        }

        if (width != null && width <= 0) {
            throw new IllegalArgumentException("Image width must be positive");
        }

        if (height != null && height <= 0) {
            throw new IllegalArgumentException("Image height must be positive");
        }
    }

    @Override
    public Map<String, Object> getMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("fileSize", fileSize);
        metadata.put("width", width);
        metadata.put("height", height);
        metadata.put("mimeType", mimeType);
        metadata.put("hasCaption", caption != null && !caption.isEmpty());
        return metadata;
    }

    @Override
    public String getSummary(int maxLength) {
        if (caption != null && !caption.isEmpty()) {
            if (caption.length() <= maxLength) {
                return "[이미지] " + caption;
            }
            return "[이미지] " + caption.substring(0, maxLength - 8) + "...";
        }
        return "[이미지]";
    }

    /**
     * JSON에서 ImageMessageContent 생성
     */
    public static ImageMessageContent fromJson(String json) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = OBJECT_MAPPER.readValue(json, Map.class);

            return ImageMessageContent.builder()
                    .imageUrl((String) data.get("imageUrl"))
                    .thumbnailUrl((String) data.get("thumbnailUrl"))
                    .fileSize(data.get("fileSize") != null ? ((Number) data.get("fileSize")).longValue() : null)
                    .width(data.get("width") != null ? ((Number) data.get("width")).intValue() : null)
                    .height(data.get("height") != null ? ((Number) data.get("height")).intValue() : null)
                    .mimeType((String) data.get("mimeType"))
                    .caption((String) data.get("caption"))
                    .build();
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to deserialize image message content", e);
        }
    }
}
