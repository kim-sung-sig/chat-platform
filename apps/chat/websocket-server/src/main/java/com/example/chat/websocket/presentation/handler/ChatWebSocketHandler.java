package com.example.chat.websocket.presentation.handler;

import java.time.Instant;
import java.util.Map;

import com.example.chat.websocket.infrastructure.redis.TypingEvent;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.example.chat.websocket.domain.session.ChatRoomSessionManager;
import com.example.chat.websocket.domain.session.ChatSession;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    private static final String READ_RECEIPT_TYPE = "READ_RECEIPT";
    private static final String READ_CHANNEL_PREFIX = "chat:read:";
    private static final String TYPING_START_TYPE = "TYPING_START";
    private static final String TYPING_STOP_TYPE = "TYPING_STOP";
    private static final String TYPING_CHANNEL_PREFIX = "chat:typing:";

    private final ChatRoomSessionManager sessionManager;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

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
     * 클라이언트 메시지 수신
     *
     * 지원 메시지 타입:
     * - READ_RECEIPT: 읽음 처리 이벤트 → Redis chat:read:{channelId} 발행
     *   페이로드: {"type":"READ_RECEIPT","channelId":"...","lastReadMessageId":"..."}
     */
    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.debug("Received WebSocket message: sessionId={}, payload={}", session.getId(), payload);

        try {
            JsonNode node = objectMapper.readTree(payload);
            String type = node.path("type").asText(null);

            if (READ_RECEIPT_TYPE.equals(type)) {
                handleReadReceipt(session, node);
            } else if (TYPING_START_TYPE.equals(type) || TYPING_STOP_TYPE.equals(type)) {
                handleTypingEvent(session, node, type);
            } else {
                log.debug("Ignored unsupported message type: {}", type);
            }
        } catch (Exception e) {
            log.warn("Failed to parse WebSocket message: sessionId={}, payload={}", session.getId(), payload, e);
        }
    }

    /**
     * 읽음 이벤트 처리
     * 클라이언트 → websocket-server → Redis(chat:read:{channelId}) → chat-server
     */
    private void handleReadReceipt(WebSocketSession session, JsonNode node) {
        String channelId = node.path("channelId").asText(null);
        String lastReadMessageId = node.path("lastReadMessageId").asText(null);
        String userId = extractUserIdAsString(session);

        if (channelId == null || lastReadMessageId == null || userId == null) {
            log.warn("Invalid READ_RECEIPT: missing required fields (channelId={}, messageId={}, userId={})",
                    channelId, lastReadMessageId, userId);
            return;
        }

        try {
            // chat-server가 구독하는 채널로 읽음 이벤트 발행
            String redisPayload = objectMapper.writeValueAsString(
                    Map.of("type", READ_RECEIPT_TYPE,
                            "userId", userId,
                            "channelId", channelId,
                            "lastReadMessageId", lastReadMessageId));
            redisTemplate.convertAndSend(READ_CHANNEL_PREFIX + channelId, redisPayload);
            log.debug("Read receipt forwarded to Redis: userId={}, channelId={}, messageId={}",
                    userId, channelId, lastReadMessageId);
        } catch (Exception e) {
            log.error("Failed to forward read receipt: channelId={}, userId={}", channelId, userId, e);
        }
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
            try {
                session.close(CloseStatus.SERVER_ERROR);
            } catch (Exception e) {
                log.error("Failed to close session on transport error", e);
            }
        }
    }

    /**
     * 타이핑 이벤트 처리
     * 클라이언트 → websocket-server → Redis(chat:typing:{channelId}) → 전체 채널 브로드캐스트
     *
     * @param session WebSocket 세션
     * @param node    파싱된 JSON 노드
     * @param type    "TYPING_START" | "TYPING_STOP"
     */
    private void handleTypingEvent(WebSocketSession session, JsonNode node, String type) {
        String channelId = node.path("channelId").asText(null);
        String userId = extractUserIdAsString(session);

        if (channelId == null || userId == null) {
            log.warn("Invalid typing event: missing channelId or userId (channelId={}, userId={})",
                    channelId, userId);
            return;
        }

        try {
            TypingEvent event = TypingEvent.builder()
                    .eventType(type)
                    .channelId(channelId)
                    .senderId(userId)
                    .build();

            String redisPayload = objectMapper.writeValueAsString(event);
            redisTemplate.convertAndSend(TYPING_CHANNEL_PREFIX + channelId, redisPayload);

            log.debug("Typing event published: type={}, channelId={}, userId={}", type, channelId, userId);
        } catch (Exception e) {
            log.error("Failed to handle typing event: type={}, channelId={}, userId={}", type, channelId, userId, e);
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
     * 세션에서 userId를 String으로 추출 (UUID 지원)
     */
    private String extractUserIdAsString(WebSocketSession session) {
        Object userId = session.getAttributes().get("userId");
        return userId != null ? userId.toString() : null;
    }

    /**
     * 세션에서 userId 추출 (Long 파싱 시도, 실패 시 null)
     */
    private Long extractUserId(WebSocketSession session) {
        Map<String, Object> attributes = session.getAttributes();
        Object userId = attributes.get("userId");

        if (userId instanceof Long longValue) {
            return longValue;
        }

        if (userId instanceof String stringValue) {
            try {
                return Long.parseLong(stringValue);
            } catch (NumberFormatException e) {
                log.debug("userId is not a Long (UUID format): {}", userId);
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
