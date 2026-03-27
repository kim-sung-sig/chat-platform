package com.example.chat.websocket.application.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.WebSocketSession;

import com.example.chat.websocket.domain.session.ChatRoomSessionManager;
import com.example.chat.websocket.domain.session.ChatSession;
import com.example.chat.websocket.infrastructure.redis.TypingEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
@DisplayName("WebSocketBroadcastService — broadcastTypingEvent()")
class WebSocketBroadcastServiceTypingTest {

    @Mock private ChatRoomSessionManager sessionManager;
    @Mock private WebSocketSession ws1;
    @Mock private WebSocketSession ws2;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private WebSocketBroadcastService broadcastService;

    @BeforeEach
    void setUp() {
        broadcastService = new WebSocketBroadcastService(sessionManager, objectMapper);
    }

    // -----------------------------------------------------------------------
    // Helper: 활성 세션 스텁 생성
    // -----------------------------------------------------------------------
    private ChatSession activeSession(String sessionId, WebSocketSession ws) {
        return ChatSession.builder()
                .sessionId(sessionId)
                .webSocketSession(ws)
                .build();
    }

    // =======================================================================
    @Nested
    @DisplayName("broadcastTypingEvent()")
    class BroadcastTypingEvent {

        @Nested
        @DisplayName("정상 케이스")
        class HappyPath {

            @Test
            @DisplayName("채팅방에 활성 세션이 있으면 타이핑 이벤트를 모든 세션에 전송한다")
            void broadcasts_to_all_active_sessions() throws Exception {
                // Given
                TypingEvent event = TypingEvent.builder()
                        .eventType("TYPING_START")
                        .channelId("ch-123")
                        .senderId("user-456")
                        .build();
                when(sessionManager.getActiveSessionsByRoom("ch-123"))
                        .thenReturn(List.of(
                                activeSession("s1", ws1),
                                activeSession("s2", ws2)));
                when(ws1.isOpen()).thenReturn(true);
                when(ws2.isOpen()).thenReturn(true);

                // When
                broadcastService.broadcastTypingEvent("ch-123", event);

                // Then
                verify(ws1).sendMessage(any());
                verify(ws2).sendMessage(any());
            }

            @Test
            @DisplayName("TYPING_STOP 이벤트도 동일하게 브로드캐스트된다")
            void broadcasts_typing_stop() throws Exception {
                // Given
                TypingEvent event = TypingEvent.builder()
                        .eventType("TYPING_STOP")
                        .channelId("ch-123")
                        .senderId("user-456")
                        .build();
                when(sessionManager.getActiveSessionsByRoom("ch-123"))
                        .thenReturn(List.of(activeSession("s1", ws1)));
                when(ws1.isOpen()).thenReturn(true);

                // When
                broadcastService.broadcastTypingEvent("ch-123", event);

                // Then
                verify(ws1).sendMessage(any());
            }
        }

        @Nested
        @DisplayName("경계 케이스")
        class Boundary {

            @Test
            @DisplayName("활성 세션이 없으면 전송하지 않는다")
            void no_sessions_no_send() throws Exception {
                // Given
                TypingEvent event = TypingEvent.builder()
                        .eventType("TYPING_START")
                        .channelId("ch-empty")
                        .senderId("user-456")
                        .build();
                when(sessionManager.getActiveSessionsByRoom("ch-empty"))
                        .thenReturn(List.of());

                // When
                broadcastService.broadcastTypingEvent("ch-empty", event);

                // Then
                verify(ws1, never()).sendMessage(any());
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class Failure {

            @Test
            @DisplayName("roomId가 null이면 전송하지 않는다")
            void null_room_id_skips() {
                // Given
                TypingEvent event = TypingEvent.builder()
                        .eventType("TYPING_START")
                        .channelId("ch-123")
                        .senderId("user-456")
                        .build();

                // When
                broadcastService.broadcastTypingEvent(null, event);

                // Then
                verify(sessionManager, never()).getActiveSessionsByRoom(any());
            }

            @Test
            @DisplayName("typingEvent가 null이면 전송하지 않는다")
            void null_event_skips() {
                // When
                broadcastService.broadcastTypingEvent("ch-123", null);

                // Then
                verify(sessionManager, never()).getActiveSessionsByRoom(any());
            }
        }
    }
}
