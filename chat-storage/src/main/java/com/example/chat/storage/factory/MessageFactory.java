package com.example.chat.storage.factory;

import com.example.chat.common.auth.model.UserId;
import com.example.chat.storage.domain.message.Message;
import com.example.chat.storage.domain.message.MessageContent;
import com.example.chat.storage.domain.message.MessageType;
import com.example.chat.storage.handler.MessageHandler;
import com.example.chat.storage.handler.MessageHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 메시지 팩토리
 * 팩토리 패턴 - 메시지 생성 로직 캡슐화
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageFactory {

    private final MessageHandlerRegistry handlerRegistry;

    /**
     * 메시지 생성
     *
     * @param roomId 채팅방 ID
     * @param channelId 채널 ID (선택)
     * @param senderId 발신자 ID
     * @param messageType 메시지 타입
     * @param payload 메시지 페이로드
     * @return 생성된 Message
     */
    public Message createMessage(
            String roomId,
            String channelId,
            UserId senderId,
            MessageType messageType,
            Map<String, Object> payload
    ) {
        log.debug("Creating message: roomId={}, channelId={}, senderId={}, type={}",
            roomId, channelId, senderId, messageType);

        // 1. 적절한 핸들러 조회
        MessageHandler handler = handlerRegistry.getHandler(messageType);

        // 2. Payload에서 MessageContent 파싱
        MessageContent content = handler.parseContent(payload);

        // 3. MessageContent 검증
        handler.validateContent(content);

        // 4. Message 생성
        Message message = Message.create(roomId, channelId, senderId, messageType, content);

        log.debug("Message created successfully: messageType={}", messageType);

        return message;
    }

    /**
     * 답장 메시지 생성
     *
     * @param roomId 채팅방 ID
     * @param channelId 채널 ID (선택)
     * @param senderId 발신자 ID
     * @param messageType 메시지 타입
     * @param payload 메시지 페이로드
     * @param replyToMessageId 답장할 메시지 ID
     * @return 생성된 Message
     */
    public Message createReplyMessage(
            String roomId,
            String channelId,
            UserId senderId,
            MessageType messageType,
            Map<String, Object> payload,
            Long replyToMessageId
    ) {
        log.debug("Creating reply message: roomId={}, replyToMessageId={}, type={}",
            roomId, replyToMessageId, messageType);

        // 1. 적절한 핸들러 조회
        MessageHandler handler = handlerRegistry.getHandler(messageType);

        // 2. Payload에서 MessageContent 파싱
        MessageContent content = handler.parseContent(payload);

        // 3. MessageContent 검증
        handler.validateContent(content);

        // 4. 답장 Message 생성
        Message message = Message.createReply(
            roomId, channelId, senderId, messageType, content, replyToMessageId
        );

        log.debug("Reply message created successfully: replyToMessageId={}", replyToMessageId);

        return message;
    }

    /**
     * 메시지 타입이 지원되는지 확인
     */
    public boolean isMessageTypeSupported(MessageType type) {
        return handlerRegistry.isSupported(type);
    }
}
