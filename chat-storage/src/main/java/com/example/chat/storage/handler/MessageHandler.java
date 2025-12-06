package com.example.chat.storage.handler;

import com.example.chat.storage.domain.message.Message;
import com.example.chat.storage.domain.message.MessageContent;
import com.example.chat.storage.domain.message.MessageType;

import java.util.Map;

/**
 * 메시지 핸들러 인터페이스
 * 전략 패턴 - 메시지 타입별로 다른 처리 로직 적용
 */
public interface MessageHandler {

    /**
     * 지원하는 메시지 타입 확인
     */
    boolean supports(MessageType type);

    /**
     * Payload에서 MessageContent 파싱
     * @param payload 원시 메시지 데이터
     * @return 파싱된 MessageContent
     */
    MessageContent parseContent(Map<String, Object> payload);

    /**
     * MessageContent 검증
     * @param content 검증할 콘텐츠
     */
    void validateContent(MessageContent content);

    /**
     * 메시지 저장 전 처리
     * @param message 저장할 메시지
     */
    void processBeforeSave(Message message);

    /**
     * 메시지 저장 후 처리
     * @param message 저장된 메시지
     */
    void processAfterSave(Message message);
}
