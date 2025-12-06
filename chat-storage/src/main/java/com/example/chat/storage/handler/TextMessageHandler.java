package com.example.chat.storage.handler;

import com.example.chat.storage.domain.message.Message;
import com.example.chat.storage.domain.message.MessageContent;
import com.example.chat.storage.domain.message.MessageType;
import com.example.chat.storage.domain.message.content.TextMessageContent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 텍스트 메시지 핸들러
 */
@Slf4j
@Component
public class TextMessageHandler implements MessageHandler {

    @Override
    public boolean supports(MessageType type) {
        return type == MessageType.TEXT;
    }

    @Override
    public MessageContent parseContent(Map<String, Object> payload) {
        if (payload == null) {
            throw new IllegalArgumentException("Payload cannot be null");
        }

        Object textObj = payload.get("text");
        if (textObj == null) {
            throw new IllegalArgumentException("Text field is required in payload");
        }

        String text = textObj.toString();

        return TextMessageContent.builder()
                .text(text)
                .build();
    }

    @Override
    public void validateContent(MessageContent content) {
        if (!(content instanceof TextMessageContent)) {
            throw new IllegalArgumentException(
                "Content must be TextMessageContent, but was: " + content.getClass().getName()
            );
        }

        content.validate();
    }

    @Override
    public void processBeforeSave(Message message) {
        log.debug("Processing text message before save: messageId={}", message.getId());

        // 텍스트 메시지는 특별한 전처리가 필요 없음
        // 필요시 욕설 필터링, 링크 검증 등 추가 가능
    }

    @Override
    public void processAfterSave(Message message) {
        log.debug("Processing text message after save: messageId={}", message.getId());

        // 텍스트 메시지는 특별한 후처리가 필요 없음
        // 필요시 검색 인덱싱, 분석 이벤트 발행 등 추가 가능
    }
}
