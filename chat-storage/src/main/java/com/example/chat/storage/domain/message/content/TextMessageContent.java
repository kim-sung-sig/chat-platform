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
 * 텍스트 메시지 콘텐츠
 */
@Getter
@Builder
public class TextMessageContent implements MessageContent {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final int MAX_TEXT_LENGTH = 5000;

    private final String text;

    @Override
    public MessageType getType() {
        return MessageType.TEXT;
    }

    @Override
    public String toJson() {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("text", text);
            return OBJECT_MAPPER.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize text message content", e);
        }
    }

    @Override
    public void validate() {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Text message content cannot be empty");
        }

        if (text.length() > MAX_TEXT_LENGTH) {
            throw new IllegalArgumentException(
                String.format("Text message content exceeds max length: %d > %d",
                    text.length(), MAX_TEXT_LENGTH)
            );
        }
    }

    @Override
    public Map<String, Object> getMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("length", text != null ? text.length() : 0);
        metadata.put("hasEmoji", containsEmoji(text));
        return metadata;
    }

    @Override
    public String getSummary(int maxLength) {
        if (text == null) {
            return "";
        }

        if (text.length() <= maxLength) {
            return text;
        }

        return text.substring(0, maxLength - 3) + "...";
    }

    /**
     * JSON에서 TextMessageContent 생성
     */
    public static TextMessageContent fromJson(String json) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = OBJECT_MAPPER.readValue(json, Map.class);
            String text = (String) data.get("text");
            return TextMessageContent.builder()
                    .text(text)
                    .build();
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to deserialize text message content", e);
        }
    }

    /**
     * 이모지 포함 여부 확인 (간단한 구현)
     */
    private boolean containsEmoji(String text) {
        if (text == null) {
            return false;
        }

        // 간단한 이모지 감지 (유니코드 범위 기반)
        return text.codePoints().anyMatch(codePoint ->
            (codePoint >= 0x1F600 && codePoint <= 0x1F64F) || // Emoticons
            (codePoint >= 0x1F300 && codePoint <= 0x1F5FF) || // Misc Symbols
            (codePoint >= 0x1F680 && codePoint <= 0x1F6FF) || // Transport
            (codePoint >= 0x2600 && codePoint <= 0x26FF)      // Misc symbols
        );
    }
}
