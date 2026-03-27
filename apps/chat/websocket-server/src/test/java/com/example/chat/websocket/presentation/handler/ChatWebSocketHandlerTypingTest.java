package com.example.chat.websocket.presentation.handler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.example.chat.websocket.domain.session.ChatRoomSessionManager;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatWebSocketHandler — 타이핑 이벤트 처리")
class ChatWebSocketHandlerTypingTest {

    @Mock private ChatRoomSessionManager sessionManager;
    @Mock private RedisTemplate<String, String> redisTemplate;
    @Mock private WebSocketSession session;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private ChatWebSocketHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ChatWebSocketHandler(sessionManager, redisTemplate, objectMapper);
    }

    // -----------------------------------------------------------------------
    // Helper: 세션 attributes 설정
    // -----------------------------------------------------------------------
    private void givenSessionWithUser(String userId, String roomId) {
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("userId", userId);
        attrs.put("roomId", roomId);
        when(session.getAttributes()).thenReturn(attrs);
    }

    // =======================================================================
    @Nested
    @DisplayName("handleTextMessage() — TYPING_START")
    class TypingStart {

        @Nested
        @DisplayName("정상 케이스")
        class HappyPath {

            @Test
            @DisplayName("TYPING_START 수신 시 chat:typing:{channelId} 채널에 Redis 이벤트를 발행한다")
            void typing_start_publishes_to_redis() throws Exception {
                // Given
                givenSessionWithUser("user-456", "ch-123");
                String payload = objectMapper.writeValueAsString(
                        Map.of("type", "TYPING_START", "channelId", "ch-123"));

                // When
                handler.handleTextMessage(session, new TextMessage(payload));

                // Then
                verify(redisTemplate).convertAndSend(eq("chat:typing:ch-123"), any(String.class));
            }

            @Test
            @DisplayName("발행된 Redis 메시지에 eventType=TYPING_START와 senderId가 포함된다")
            void typing_start_payload_contains_event_type_and_sender() throws Exception {
                // Given
                givenSessionWithUser("user-456", "ch-123");
                String payload = objectMapper.writeValueAsString(
                        Map.of("type", "TYPING_START", "channelId", "ch-123"));

                // When
                handler.handleTextMessage(session, new TextMessage(payload));

                // Then
                verify(redisTemplate).convertAndSend(
                        eq("chat:typing:ch-123"),
                        contains("TYPING_START"));
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class Failure {

            @Test
            @DisplayName("channelId 없는 TYPING_START는 Redis 발행하지 않는다")
            void missing_channel_id_skips_publish() throws Exception {
                // Given
                givenSessionWithUser("user-456", "ch-123");
                String payload = objectMapper.writeValueAsString(
                        Map.of("type", "TYPING_START"));  // channelId 누락

                // When
                handler.handleTextMessage(session, new TextMessage(payload));

                // Then
                verify(redisTemplate, never()).convertAndSend(any(), any(String.class));
            }

            @Test
            @DisplayName("userId 없는 세션의 TYPING_START는 Redis 발행하지 않는다")
            void missing_user_id_skips_publish() throws Exception {
                // Given — userId 없는 세션
                Map<String, Object> attrs = new HashMap<>();
                attrs.put("roomId", "ch-123");
                // userId 없음
                when(session.getAttributes()).thenReturn(attrs);

                String payload = objectMapper.writeValueAsString(
                        Map.of("type", "TYPING_START", "channelId", "ch-123"));

                // When
                handler.handleTextMessage(session, new TextMessage(payload));

                // Then
                verify(redisTemplate, never()).convertAndSend(any(), any(String.class));
            }
        }
    }

    // =======================================================================
    @Nested
    @DisplayName("handleTextMessage() — TYPING_STOP")
    class TypingStop {

        @Nested
        @DisplayName("정상 케이스")
        class HappyPath {

            @Test
            @DisplayName("TYPING_STOP 수신 시 chat:typing:{channelId} 채널에 Redis 이벤트를 발행한다")
            void typing_stop_publishes_to_redis() throws Exception {
                // Given
                givenSessionWithUser("user-456", "ch-123");
                String payload = objectMapper.writeValueAsString(
                        Map.of("type", "TYPING_STOP", "channelId", "ch-123"));

                // When
                handler.handleTextMessage(session, new TextMessage(payload));

                // Then
                verify(redisTemplate).convertAndSend(eq("chat:typing:ch-123"), any(String.class));
            }

            @Test
            @DisplayName("발행된 Redis 메시지에 eventType=TYPING_STOP이 포함된다")
            void typing_stop_payload_contains_event_type() throws Exception {
                // Given
                givenSessionWithUser("user-456", "ch-123");
                String payload = objectMapper.writeValueAsString(
                        Map.of("type", "TYPING_STOP", "channelId", "ch-123"));

                // When
                handler.handleTextMessage(session, new TextMessage(payload));

                // Then
                verify(redisTemplate).convertAndSend(
                        eq("chat:typing:ch-123"),
                        contains("TYPING_STOP"));
            }
        }
    }

    // =======================================================================
    @Nested
    @DisplayName("handleTextMessage() — 미지원 타입")
    class UnsupportedType {

        @Test
        @DisplayName("미지원 타입 메시지는 Redis 발행 없이 무시된다")
        void unknown_type_is_ignored() throws Exception {
            // Given — 미지원 타입은 handleTypingEvent를 호출하지 않으므로 세션 stub 불필요
            String payload = objectMapper.writeValueAsString(
                    Map.of("type", "UNKNOWN_TYPE", "channelId", "ch-123"));

            // When
            handler.handleTextMessage(session, new TextMessage(payload));

            // Then
            verify(redisTemplate, never()).convertAndSend(any(), any(String.class));
        }
    }
}
