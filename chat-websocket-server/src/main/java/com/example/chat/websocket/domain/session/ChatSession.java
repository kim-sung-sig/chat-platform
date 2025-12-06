package com.example.chat.websocket.domain.session;

import lombok.Builder;
import lombok.Getter;
import org.springframework.web.socket.WebSocketSession;

import java.time.Instant;
import java.util.Objects;

/**
 * 채팅 세션 도메인
 * WebSocket 세션과 사용자 정보를 관리
 */
@Getter
@Builder
public class ChatSession {

    private final String sessionId;
    private final Long userId;
    private final String roomId;
    private final WebSocketSession webSocketSession;
    private final Instant connectedAt;

    /**
     * 세션이 활성 상태인지 확인
     */
    public boolean isActive() {
        return webSocketSession != null && webSocketSession.isOpen();
    }

    /**
     * 세션 ID로 동일성 비교
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatSession that = (ChatSession) o;
        return Objects.equals(sessionId, that.sessionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId);
    }
}
