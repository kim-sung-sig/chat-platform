package com.example.chat.storage.handler;

import com.example.chat.storage.domain.message.Message;
import com.example.chat.storage.domain.message.MessageContent;
import com.example.chat.storage.domain.message.MessageType;
import com.example.chat.storage.domain.message.content.ImageMessageContent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 이미지 메시지 핸들러
 */
@Slf4j
@Component
public class ImageMessageHandler implements MessageHandler {

    @Override
    public boolean supports(MessageType type) {
        return type == MessageType.IMAGE;
    }

    @Override
    public MessageContent parseContent(Map<String, Object> payload) {
        if (payload == null) {
            throw new IllegalArgumentException("Payload cannot be null");
        }

        String imageUrl = (String) payload.get("imageUrl");
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("imageUrl is required in payload");
        }

        String thumbnailUrl = (String) payload.get("thumbnailUrl");
        Long fileSize = payload.get("fileSize") != null ?
            ((Number) payload.get("fileSize")).longValue() : null;
        Integer width = payload.get("width") != null ?
            ((Number) payload.get("width")).intValue() : null;
        Integer height = payload.get("height") != null ?
            ((Number) payload.get("height")).intValue() : null;
        String mimeType = (String) payload.get("mimeType");
        String caption = (String) payload.get("caption");

        return ImageMessageContent.builder()
                .imageUrl(imageUrl)
                .thumbnailUrl(thumbnailUrl)
                .fileSize(fileSize)
                .width(width)
                .height(height)
                .mimeType(mimeType)
                .caption(caption)
                .build();
    }

    @Override
    public void validateContent(MessageContent content) {
        if (!(content instanceof ImageMessageContent)) {
            throw new IllegalArgumentException(
                "Content must be ImageMessageContent, but was: " + content.getClass().getName()
            );
        }

        content.validate();
    }

    @Override
    public void processBeforeSave(Message message) {
        log.debug("Processing image message before save: messageId={}", message.getId());

        // 이미지 URL 검증, 썸네일 생성 요청 등
        // 실제 구현에서는 비동기로 처리하는 것이 좋음
    }

    @Override
    public void processAfterSave(Message message) {
        log.debug("Processing image message after save: messageId={}", message.getId());

        // 이미지 최적화 작업 트리거, CDN 업로드 등
        // 실제 구현에서는 이벤트 발행으로 처리
    }
}
