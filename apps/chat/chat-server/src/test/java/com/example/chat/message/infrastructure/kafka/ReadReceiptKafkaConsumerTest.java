package com.example.chat.message.infrastructure.kafka;

import java.time.Instant;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import static org.assertj.core.api.Assertions.assertThatNoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import com.example.chat.storage.repository.JpaMessageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * [단위 테스트] ReadReceiptKafkaConsumer
 *
 * 검증 범위:
 * - 정상 이벤트 → bulkDecrementUnreadCountBeforeCursor 호출 + ACK
 * - null/invalid 이벤트 → 스킵(ACK), 메서드 호출 없음
 * - JSON 파싱 실패 → ACK 처리 후 예외 전파 없음 (무한루프 방지)
 * - lastReadCreatedAt null → 유효성 검사 후 스킵
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReadReceiptKafkaConsumer 단위 테스트")
class ReadReceiptKafkaConsumerTest {

    @Mock
    JpaMessageRepository messageRepository;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    Acknowledgment acknowledgment;

    @InjectMocks
    ReadReceiptKafkaConsumer consumer;

    private static final String CHANNEL_ID  = "channel-001";
    private static final String USER_ID     = "user-001";
    private static final String MESSAGE_ID  = "msg-001";
    private static final Instant CURSOR     = Instant.parse("2026-03-12T10:00:00Z");
    private static final String VALID_JSON  =
            "{\"userId\":\"user-001\",\"channelId\":\"channel-001\"," +
            "\"lastReadMessageId\":\"msg-001\",\"lastReadCreatedAt\":\"2026-03-12T10:00:00Z\"}";

    private ConsumerRecord<String, String> record(String value) {
        return new ConsumerRecord<>("read-receipt-events", 0, 0L, CHANNEL_ID, value);
    }

    // ──────────────────────────────────────────────────
    // Happy Path
    // ──────────────────────────────────────────────────
    @Nested
    @DisplayName("정상 처리")
    class HappyPath {

        @BeforeEach
        void setUp() throws Exception {
            ReadReceiptKafkaEvent event = new ReadReceiptKafkaEvent(USER_ID, CHANNEL_ID, MESSAGE_ID, CURSOR);
            when(objectMapper.readValue(VALID_JSON, ReadReceiptKafkaEvent.class)).thenReturn(event);
        }

        @Test
        @DisplayName("bulkDecrementUnreadCountBeforeCursor 가 올바른 인자로 호출된다")
        void shouldCallBulkDecrement() throws Exception {
            when(messageRepository.bulkDecrementUnreadCountBeforeCursor(CHANNEL_ID, CURSOR)).thenReturn(5);

            consumer.consume(record(VALID_JSON), acknowledgment);

            verify(messageRepository).bulkDecrementUnreadCountBeforeCursor(CHANNEL_ID, CURSOR);
        }

        @Test
        @DisplayName("정상 처리 후 ACK 를 호출한다")
        void shouldAcknowledgeAfterSuccess() throws Exception {
            when(messageRepository.bulkDecrementUnreadCountBeforeCursor(eq(CHANNEL_ID), eq(CURSOR))).thenReturn(3);

            consumer.consume(record(VALID_JSON), acknowledgment);

            verify(acknowledgment, times(1)).acknowledge();
        }

        @Test
        @DisplayName("업데이트 건수 0 이어도 ACK 를 호출한다")
        void shouldAcknowledgeEvenWhenZeroUpdated() throws Exception {
            when(messageRepository.bulkDecrementUnreadCountBeforeCursor(eq(CHANNEL_ID), eq(CURSOR))).thenReturn(0);

            consumer.consume(record(VALID_JSON), acknowledgment);

            verify(acknowledgment, times(1)).acknowledge();
        }
    }

    // ──────────────────────────────────────────────────
    // Invalid Event
    // ──────────────────────────────────────────────────
    @Nested
    @DisplayName("유효하지 않은 이벤트")
    class InvalidEvent {

        @Test
        @DisplayName("channelId null 이면 DB 호출 없이 ACK")
        void shouldSkipWhenChannelIdNull() throws Exception {
            ReadReceiptKafkaEvent nullChannel = new ReadReceiptKafkaEvent(USER_ID, null, MESSAGE_ID, CURSOR);
            when(objectMapper.readValue(anyString(), eq(ReadReceiptKafkaEvent.class))).thenReturn(nullChannel);

            consumer.consume(record("{}"), acknowledgment);

            verifyNoInteractions(messageRepository);
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("lastReadCreatedAt null 이면 DB 호출 없이 ACK")
        void shouldSkipWhenCursorNull() throws Exception {
            ReadReceiptKafkaEvent nullCursor = new ReadReceiptKafkaEvent(USER_ID, CHANNEL_ID, MESSAGE_ID, null);
            when(objectMapper.readValue(anyString(), eq(ReadReceiptKafkaEvent.class))).thenReturn(nullCursor);

            consumer.consume(record("{}"), acknowledgment);

            verifyNoInteractions(messageRepository);
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("역직렬화가 null 반환이면 ACK")
        void shouldSkipWhenDeserializationReturnsNull() throws Exception {
            when(objectMapper.readValue(anyString(), eq(ReadReceiptKafkaEvent.class))).thenReturn(null);

            consumer.consume(record("null_json"), acknowledgment);

            verifyNoInteractions(messageRepository);
            verify(acknowledgment).acknowledge();
        }
    }

    // ──────────────────────────────────────────────────
    // Exception Handling
    // ──────────────────────────────────────────────────
    @Nested
    @DisplayName("예외 처리 — 무한 루프 방지")
    class ExceptionHandling {

        @Test
        @DisplayName("JSON 파싱 예외 발생 시 예외 전파 없이 ACK")
        void shouldNotPropagateJsonParseException() throws Exception {
            when(objectMapper.readValue(anyString(), eq(ReadReceiptKafkaEvent.class)))
                    .thenThrow(new com.fasterxml.jackson.core.JsonParseException(null, "bad json"));

            assertThatNoException().isThrownBy(() -> consumer.consume(record("bad"), acknowledgment));
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("DB 예외 발생 시 예외 전파 없이 ACK")
        void shouldNotPropagateDbException() throws Exception {
            ReadReceiptKafkaEvent event = new ReadReceiptKafkaEvent(USER_ID, CHANNEL_ID, MESSAGE_ID, CURSOR);
            when(objectMapper.readValue(anyString(), eq(ReadReceiptKafkaEvent.class))).thenReturn(event);
            when(messageRepository.bulkDecrementUnreadCountBeforeCursor(any(), any()))
                    .thenThrow(new RuntimeException("DB down"));

            assertThatNoException().isThrownBy(() -> consumer.consume(record(VALID_JSON), acknowledgment));
            verify(acknowledgment).acknowledge();
        }
    }
}
