package com.example.chat.common.port;

import com.example.chat.common.dto.ChatMessage;

import java.util.List;

/**
 * 메시지 도메인의 핵심 유스케이스를 노출하는 포트 인터페이스.
 * 구현체는 message-server 또는 storage-adapter(영속화)에서 제공할 수 있습니다.
 */
public interface MessageService {

    /**
     * 메시지 전송 처리(동기/비동기 내부 구현에 따라 다름).
     */
    void sendMessage(ChatMessage message);

    /**
     * 커서 기반 페이징: cursor는 마지막으로 본 메시지의 cursor (createdAt|id encoded), 역방향(과거) 조회.
     * cursor가 null이면 최신부터 조회.
     */
    List<ChatMessage> fetchMessages(String channelId, String cursor, int limit);
}