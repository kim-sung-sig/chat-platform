package com.example.chat.websocket.domain.session;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

/**
 * WebRTC 음성 채팅을 위한 인메모리 세션 관리자.
 * (P2P 메시지 릴레이만을 위한 임시 세션 풀)
 */
@Component
public class WebRtcSessionManager {

    // roomId -> list of sessions
    private final Map<String, List<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();

    public void addSession(String roomId, WebSocketSession session) {
        roomSessions.computeIfAbsent(roomId, k -> new CopyOnWriteArrayList<>()).add(session);
    }

    public void removeSession(String roomId, WebSocketSession session) {
        List<WebSocketSession> sessions = roomSessions.get(roomId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                roomSessions.remove(roomId);
            }
        }
    }

    public List<WebSocketSession> getSessions(String roomId) {
        return roomSessions.getOrDefault(roomId, List.of());
    }

    public void removeSessionFromAllRooms(WebSocketSession session) {
        roomSessions.values().forEach(list -> list.remove(session));
        roomSessions.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }
}
