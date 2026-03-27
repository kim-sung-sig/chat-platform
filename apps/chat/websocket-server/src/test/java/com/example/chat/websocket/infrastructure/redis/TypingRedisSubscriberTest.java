package com.example.chat.websocket.infrastructure.redis;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.Message;

import com.example.chat.websocket.application.service.WebSocketBroadcastService;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
@DisplayName("TypingRedisSubscriber")
class TypingRedisSubscriberTest {

    @Mock
    private WebSocketBroadcastService broadcastService;

    @InjectMocks
    private TypingRedisSubscriber subscriber;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // -----------------------------------------------------------------------
    // Helper: Message stub
    // -----------------------------------------------------------------------
    private Message buildMessage(String body, String channel) {
        return new Message() {
            @Override public byte[] getBody() { return body.getBytes(); }
            @Override public byte[] getChannel() { return channel.getBytes(); }
        };
    }

    // -----------------------------------------------------------------------
    // Re-create subscriber with real ObjectMapper (InjectMocks doesn't wire it)
    // -----------------------------------------------------------------------
    private TypingRedisSubscriber subscriberWithRealMapper() {
        return new TypingRedisSubscriber(objectMapper, broadcastService);
    }

    // =======================================================================
    @Nested
    @DisplayName("onMessage()")
    class OnMessage {

        @Nested
        @DisplayName("정상 케이스")
        class HappyPath {

            @Test
            @DisplayName("TYPING_START 이벤트 수신 시 broadcastTypingEvent를 호출한다")
            void typing_start_broadcasts_to_room() throws Exception {
                // Given
                String body = objectMapper.writeValueAsString(
                        TypingEvent.builder()
                                .eventType("TYPING_START")
                                .channelId("ch-123")
                                .senderId("user-456")
                                .build());
                Message message = buildMessage(body, "chat:typing:ch-123");
                TypingRedisSubscriber sub = subscriberWithRealMapper();

                // When
                sub.onMessage(message, null);

                // Then
                verify(broadcastService).broadcastTypingEvent(
                        eq("ch-123"),
                        argThat(e -> "TYPING_START".equals(e.getEventType())
                                && "user-456".equals(e.getSenderId())));
            }

            @Test
            @DisplayName("TYPING_STOP 이벤트 수신 시 broadcastTypingEvent를 호출한다")
            void typing_stop_broadcasts_to_room() throws Exception {
                // Given
                String body = objectMapper.writeValueAsString(
                        TypingEvent.builder()
                                .eventType("TYPING_STOP")
                                .channelId("ch-123")
                                .senderId("user-456")
                                .build());
                Message message = buildMessage(body, "chat:typing:ch-123");
                TypingRedisSubscriber sub = subscriberWithRealMapper();

                // When
                sub.onMessage(message, null);

                // Then
                verify(broadcastService).broadcastTypingEvent(
                        eq("ch-123"),
                        argThat(e -> "TYPING_STOP".equals(e.getEventType())));
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class Failure {

            @Test
            @DisplayName("channelId가 null인 이벤트는 브로드캐스트하지 않는다")
            void null_channel_id_skips_broadcast() throws Exception {
                // Given
                String body = objectMapper.writeValueAsString(
                        TypingEvent.builder()
                                .eventType("TYPING_START")
                                .channelId(null)
                                .senderId("user-456")
                                .build());
                Message message = buildMessage(body, "chat:typing:ch-123");
                TypingRedisSubscriber sub = subscriberWithRealMapper();

                // When
                sub.onMessage(message, null);

                // Then
                verify(broadcastService, never()).broadcastTypingEvent(any(), any());
            }

            @Test
            @DisplayName("잘못된 JSON 수신 시 브로드캐스트하지 않는다")
            void invalid_json_skips_broadcast() {
                // Given
                Message message = buildMessage("invalid-json{{{", "chat:typing:ch-123");
                TypingRedisSubscriber sub = subscriberWithRealMapper();

                // When (예외 없이 처리되어야 함)
                sub.onMessage(message, null);

                // Then
                verify(broadcastService, never()).broadcastTypingEvent(any(), any());
            }

            @Test
            @DisplayName("빈 body 수신 시 브로드캐스트하지 않는다")
            void empty_body_skips_broadcast() {
                // Given
                Message message = buildMessage("", "chat:typing:ch-123");
                TypingRedisSubscriber sub = subscriberWithRealMapper();

                // When
                sub.onMessage(message, null);

                // Then
                verify(broadcastService, never()).broadcastTypingEvent(any(), any());
            }
        }
    }
}
