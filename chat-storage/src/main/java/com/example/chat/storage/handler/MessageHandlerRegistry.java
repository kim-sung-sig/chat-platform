package com.example.chat.storage.handler;

import com.example.chat.storage.domain.message.MessageType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 메시지 핸들러 레지스트리
 * 모든 MessageHandler를 관리하고 적절한 핸들러를 라우팅
 */
@Slf4j
@Component
public class MessageHandlerRegistry {

    private final Map<MessageType, MessageHandler> handlers = new ConcurrentHashMap<>();

    /**
     * Spring이 모든 MessageHandler 빈을 주입하여 초기화
     */
    public MessageHandlerRegistry(List<MessageHandler> handlerList) {
        for (MessageHandler handler : handlerList) {
            for (MessageType type : MessageType.values()) {
                if (handler.supports(type)) {
                    MessageHandler existing = handlers.put(type, handler);
                    if (existing != null) {
                        log.warn("Duplicate handler for message type: {}. Replaced {} with {}",
                            type, existing.getClass().getSimpleName(), handler.getClass().getSimpleName());
                    }
                    log.info("Registered handler for message type {}: {}",
                        type, handler.getClass().getSimpleName());
                }
            }
        }

        log.info("MessageHandlerRegistry initialized with {} handlers", handlers.size());
    }

    /**
     * 메시지 타입에 맞는 핸들러 조회
     * @param type 메시지 타입
     * @return MessageHandler
     * @throws UnsupportedMessageTypeException 지원하지 않는 타입인 경우
     */
    public MessageHandler getHandler(MessageType type) {
        MessageHandler handler = handlers.get(type);

        if (handler == null) {
            throw new UnsupportedMessageTypeException(
                String.format("No handler registered for message type: %s", type)
            );
        }

        return handler;
    }

    /**
     * 특정 메시지 타입이 지원되는지 확인
     */
    public boolean isSupported(MessageType type) {
        return handlers.containsKey(type);
    }

    /**
     * 런타임에 핸들러 등록 (플러그인 방식 지원)
     * 주의: 동적 등록은 신중하게 사용해야 함
     */
    public void registerHandler(MessageType type, MessageHandler handler) {
        if (!handler.supports(type)) {
            throw new IllegalArgumentException(
                String.format("Handler %s does not support type %s",
                    handler.getClass().getSimpleName(), type)
            );
        }

        MessageHandler existing = handlers.put(type, handler);
        if (existing != null) {
            log.warn("Replaced handler for message type {}: {} -> {}",
                type, existing.getClass().getSimpleName(), handler.getClass().getSimpleName());
        } else {
            log.info("Dynamically registered handler for message type {}: {}",
                type, handler.getClass().getSimpleName());
        }
    }

    /**
     * 지원하지 않는 메시지 타입 예외
     */
    public static class UnsupportedMessageTypeException extends RuntimeException {
        public UnsupportedMessageTypeException(String message) {
            super(message);
        }
    }
}
