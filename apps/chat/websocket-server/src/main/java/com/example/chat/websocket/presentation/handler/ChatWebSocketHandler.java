package com.example.chat.websocket.presentation.handler;

import java.time.Instant;
import java.util.Map;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.example.chat.websocket.domain.session.ChatRoomSessionManager;
import com.example.chat.websocket.domain.session.ChatSession;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * WebSocket 핸들러
 * WebSocket 연결, 메시지 수신, 연결 종료 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ChatRoomSessionManager sessionManager;

    /**
     * WebSocket 연결 수립
     */
    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        log.info("WebSocket connection established: sessionId={}", session.getId());

        // Step 1: 세션 정보 추출
        String roomId = extractRoomId(session);
        Long userId = extractUserId(session);

        // Early return: roomId 없으면 연결 종료
        if (roomId == null) {
            log.warn("RoomId not found in session attributes. Closing connection.");
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        // Step 2: ChatSession 생성
        ChatSession chatSession = createChatSession(session, roomId, userId);

        // Step 3: 세션 등록
        sessionManager.registerSession(chatSession);

        log.info("WebSocket session registered: sessionId={}, userId={}, roomId={}",
                session.getId(), userId, roomId);
    }

    /**
     * 메시지 수신 (현재는 읽기 전용, 메시지 발송은 REST API 사용)
     */
    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
        log.debug("Received WebSocket message: sessionId={}, payload={}",
                session.getId(), message.getPayload());

        // 클라이언트에서 보낸 메시지는 무시 (메시지 발송은 REST API를 통해)
        // Ping/Pong 또는 읽음 처리 등의 가벼운 작업만 처리 가능
    }

    /**
     * WebSocket 연결 종료
     */
    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws Exception {
        log.info("WebSocket connection closed: sessionId={}, status={}",
                session.getId(), status);

        // 세션 제거
        sessionManager.removeSession(session.getId());
    }

    /**
     * 전송 에러 처리
     */
    @Override
    public void handleTransportError(@NonNull WebSocketSession session, @NonNull Throwable exception) throws Exception {
        log.error("WebSocket transport error: sessionId={}", session.getId(), exception);

        // 에러 발생 시 세션 제거
        sessionManager.removeSession(session.getId());

        // 연결 종료
        if (session.isOpen()) {
            session.close(CloseStatus.SERVER_ERROR);
        }
    }

    // ========== Private Helper Methods ==========

    /**
     * 세션에서 roomId 추출
     */
    private String extractRoomId(WebSocketSession session) {
        Map<String, Object> attributes = session.getAttributes();
        Object roomId = attributes.get("roomId");

        return roomId != null ? roomId.toString() : null;
    }

    /**
     * 세션에서 userId 추출
     */
    private Long extractUserId(WebSocketSession session) {
        Map<String, Object> attributes = session.getAttributes();
        Object userId = attributes.get("userId");

        if (userId instanceof Long) {
            return (Long) userId;
        }

        if (userId instanceof String) {
            try {
                return Long.parseLong((String) userId);
            } catch (NumberFormatException e) {
                log.warn("Failed to parse userId: {}", userId);
            }
        }

        return null;
    }

    /**
     * ChatSession 도메인 생성
     */
    private ChatSession createChatSession(WebSocketSession session, String roomId, Long userId) {
        return ChatSession.builder()
                .sessionId(session.getId())
                .userId(userId)
                .roomId(roomId)
                .webSocketSession(session)
                .connectedAt(Instant.now())
                .build();
    }
}
