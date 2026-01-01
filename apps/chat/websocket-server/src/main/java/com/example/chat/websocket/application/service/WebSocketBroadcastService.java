package com.example.chat.websocket.application.service;

import java.util.List;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.example.chat.websocket.domain.session.ChatRoomSessionManager;
import com.example.chat.websocket.domain.session.ChatSession;
import com.example.chat.websocket.infrastructure.redis.MessageEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * WebSocket 브로드캐스트 서비스
 * 채팅방의 모든 활성 세션에 메시지 전송
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketBroadcastService {

    private final ChatRoomSessionManager sessionManager;
    private final ObjectMapper objectMapper;

    /**
     * 채팅방의 모든 세션에 브로드캐스트
     */
    public void broadcastToRoom(String roomId, MessageEvent event) {
        // Early return: null 체크
        if (roomId == null || event == null) {
            log.warn("Cannot broadcast: roomId or event is null");
            return;
        }

        log.debug("Broadcasting message to room: roomId={}, messageId={}",
                roomId, event.getMessageId());

        // Step 1: 채팅방의 활성 세션 조회
        List<ChatSession> activeSessions = findActiveSessionsByRoom(roomId);

        // Early return: 활성 세션이 없으면 종료
        if (activeSessions.isEmpty()) {
            log.debug("No active sessions in room: {}", roomId);
            return;
        }

        // Step 2: 메시지를 JSON으로 직렬화
        String messageJson = serializeMessage(event);

        // Early return: 직렬화 실패
        if (messageJson == null) {
            log.error("Failed to serialize message");
            return;
        }

        // Step 3: 각 세션에 메시지 전송
        int successCount = 0;
        int failCount = 0;

        for (ChatSession session : activeSessions) {
            if (sendMessageToSession(session, messageJson)) {
                successCount++;
            } else {
                failCount++;
            }
        }

        log.info("Broadcast completed: roomId={}, messageId={}, success={}, fail={}",
                roomId, event.getMessageId(), successCount, failCount);
    }

    /**
     * 특정 사용자의 모든 세션에 전송
     */
    public void broadcastToUser(Long userId, MessageEvent event) {
        // Early return: null 체크
        if (userId == null || event == null) {
            log.warn("Cannot broadcast: userId or event is null");
            return;
        }

        log.debug("Broadcasting message to user: userId={}, messageId={}",
                userId, event.getMessageId());

        // Step 1: 사용자의 활성 세션 조회
        List<ChatSession> activeSessions = findActiveSessionsByUser(userId);

        // Early return: 활성 세션이 없으면 종료
        if (activeSessions.isEmpty()) {
            log.debug("No active sessions for user: {}", userId);
            return;
        }

        // Step 2: 메시지를 JSON으로 직렬화
        String messageJson = serializeMessage(event);

        // Early return: 직렬화 실패
        if (messageJson == null) {
            log.error("Failed to serialize message");
            return;
        }

        // Step 3: 각 세션에 메시지 전송
        int successCount = 0;
        for (ChatSession session : activeSessions) {
            if (sendMessageToSession(session, messageJson)) {
                successCount++;
            }
        }

        log.info("User broadcast completed: userId={}, messageId={}, sent={}",
                userId, event.getMessageId(), successCount);
    }

    // ========== Private Helper Methods ==========

    /**
     * Key 기반: 채팅방의 활성 세션 조회
     */
    private List<ChatSession> findActiveSessionsByRoom(String roomId) {
        return sessionManager.getActiveSessionsByRoom(roomId);
    }

    /**
     * Key 기반: 사용자의 활성 세션 조회
     */
    private List<ChatSession> findActiveSessionsByUser(Long userId) {
        return sessionManager.getActiveSessionsByUser(userId);
    }

    /**
     * 메시지 직렬화
     */
    private String serializeMessage(MessageEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            log.error("Failed to serialize message event", e);
            return null;
        }
    }

    /**
     * 세션에 메시지 전송
     */
    private boolean sendMessageToSession(ChatSession chatSession, @NonNull String messageJson) {
        // Early return: 비활성 세션
        if (!chatSession.isActive()) {
            log.debug("Session is not active: {}", chatSession.getSessionId());
            return false;
        }

        WebSocketSession session = chatSession.getWebSocketSession();

        try {
            // 동기 전송 (WebSocket은 기본적으로 동기)
            session.sendMessage(new TextMessage(messageJson));
            return true;

        } catch (Exception e) {
            log.error("Failed to send message to session: sessionId={}",
                    chatSession.getSessionId(), e);

            // 세션 제거 (연결이 끊어진 경우)
            sessionManager.removeSession(chatSession.getSessionId());
            return false;
        }
    }
}
