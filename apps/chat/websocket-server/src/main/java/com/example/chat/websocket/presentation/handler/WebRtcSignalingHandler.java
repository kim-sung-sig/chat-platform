package com.example.chat.websocket.presentation.handler;

import java.util.List;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.example.chat.websocket.domain.session.WebRtcSessionManager;
import com.example.chat.websocket.presentation.dto.WebRtcSignalingMessage;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * WebRTC 음성 채팅의 P2P 시그널링을 중계하는 WebSocket 핸들러.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebRtcSignalingHandler extends TextWebSocketHandler {

    private final WebRtcSessionManager sessionManager;
    private final ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        log.info("WebRTC Signaling WS connection established: sessionId={}", session.getId());
        // 클라이언트로부터 "join" 메시지를 수신했을 때 방에 배정합니다.
    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
        try {
            WebRtcSignalingMessage sigMsg = objectMapper.readValue(message.getPayload(), WebRtcSignalingMessage.class);
            log.debug("Received WebRTC signaling: type={}, roomId={}, senderId={}, targetId={}",
                    sigMsg.getType(), sigMsg.getRoomId(), sigMsg.getSenderId(), sigMsg.getTargetId());

            switch (sigMsg.getType()) {
                case "join":
                    // 세션에 정보 저장
                    session.getAttributes().put("userId", sigMsg.getSenderId());
                    session.getAttributes().put("roomId", sigMsg.getRoomId());
                    sessionManager.addSession(sigMsg.getRoomId(), session);

                    // 같은 방에 있는 다른 참가자들에게 새로운 참가를 알림
                    broadcastToRoom(sigMsg.getRoomId(), sigMsg, session);
                    break;
                case "leave":
                    sessionManager.removeSession(sigMsg.getRoomId(), session);
                    session.getAttributes().remove("roomId");
                    broadcastToRoom(sigMsg.getRoomId(), sigMsg, session);
                    break;
                case "offer":
                case "answer":
                case "ice":
                    // 특정 타겟(1:1)에게만 메시지(SDP, ICE) 전달
                    sendToUserInRoom(sigMsg.getRoomId(), sigMsg.getTargetId(), sigMsg);
                    break;
                default:
                    log.warn("Unknown WebRTC signaling type: {}", sigMsg.getType());
            }

        } catch (Exception e) {
            log.error("Failed to handle WebRTC signaling message", e);
        }
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws Exception {
        log.info("WebRTC Signaling WS connection closed: sessionId={}", session.getId());

        // 만약 정상적인 leave 메시지 없이 끊어졌다면, 동일 방 참가자에게 leave를 브로드캐스트
        String roomId = (String) session.getAttributes().get("roomId");
        Long userId = (Long) session.getAttributes().get("userId");

        if (roomId != null && userId != null) {
            WebRtcSignalingMessage leaveMsg = WebRtcSignalingMessage.builder()
                    .type("leave")
                    .roomId(roomId)
                    .senderId(userId)
                    .build();
            broadcastToRoom(roomId, leaveMsg, session);
        }
        sessionManager.removeSessionFromAllRooms(session);
    }

    private void broadcastToRoom(String roomId, WebRtcSignalingMessage message, WebSocketSession excludeSession) {
        List<WebSocketSession> sessions = sessionManager.getSessions(roomId);
        try {
            String payload = objectMapper.writeValueAsString(message);
            if (payload != null) {
                TextMessage textMessage = new TextMessage(payload);
                for (WebSocketSession s : sessions) {
                    if (s.isOpen() && !s.getId().equals(excludeSession.getId())) {
                        s.sendMessage(textMessage);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to broadcast WebRTC message to room: {}", roomId, e);
        }
    }

    private void sendToUserInRoom(String roomId, Long targetUserId, WebRtcSignalingMessage message) {
        if (targetUserId == null)
            return;
        List<WebSocketSession> sessions = sessionManager.getSessions(roomId);
        try {
            String payload = objectMapper.writeValueAsString(message);
            if (payload != null) {
                TextMessage textMessage = new TextMessage(payload);
                for (WebSocketSession s : sessions) {
                    if (s.isOpen()) {
                        Long sessionUserId = (Long) s.getAttributes().get("userId");
                        if (targetUserId.equals(sessionUserId)) {
                            s.sendMessage(textMessage);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to send WebRTC message to target: {}", targetUserId, e);
        }
    }
}
