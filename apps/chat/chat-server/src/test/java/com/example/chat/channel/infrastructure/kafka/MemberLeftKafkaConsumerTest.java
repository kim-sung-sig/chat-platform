package com.example.chat.channel.infrastructure.kafka;

import java.time.Instant;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import static org.assertj.core.api.Assertions.assertThatNoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import com.example.chat.storage.domain.repository.JpaChannelMetadataRepository;
import com.example.chat.storage.domain.repository.JpaMessageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * [단위 테스트] MemberLeftKafkaConsumer
 *
 * 검증 범위:
 * - lastReadAt 있음 → bulkDecrementUnreadCountAfterCursor 호출 + deleteByChannelIdAndUserId + ACK
 * - lastReadAt null(한 번도 읽지 않음) → 전체 메시지 감소 + ACK
 * - channelId / userId null → 스킵(ACK)
 * - JSON 파싱 / DB 예외 → 예외 전파 없이 ACK
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MemberLeftKafkaConsumer 단위 테스트")
class MemberLeftKafkaConsumerTest {

    @Mock JpaMessageRepository messageRepository;
    @Mock JpaChannelMetadataRepository metadataRepository;
    @Mock ObjectMapper objectMapper;
    @Mock Acknowledgment acknowledgment;

    @InjectMocks MemberLeftKafkaConsumer consumer;

    private static final String CHANNEL_ID = "ch-001";
    private static final String USER_ID    = "u-001";
    private static final Instant LAST_READ = Instant.parse("2026-03-10T09:00:00Z");

    private ConsumerRecord<String, String> record(String value) {
        return new ConsumerRecord<>("member-left-events", 0, 0L, CHANNEL_ID, value);
    }

    // ──────────────────────────────────────────────────
    // Happy Path
    // ──────────────────────────────────────────────────
    @Nested
    @DisplayName("정상 처리")
    class HappyPath {

        @Test
        @DisplayName("lastReadAt 있음 → bulkDecrement + deleteMetadata + ACK")
        void shouldDecrementAndDeleteWhenLastReadAtPresent() throws Exception {
            MemberLeftKafkaEvent event = new MemberLeftKafkaEvent(USER_ID, CHANNEL_ID, LAST_READ);
            when(objectMapper.readValue(anyString(), eq(MemberLeftKafkaEvent.class))).thenReturn(event);
            when(messageRepository.bulkDecrementUnreadCountAfterCursor(CHANNEL_ID, LAST_READ)).thenReturn(7);

            consumer.consume(record("{}"), acknowledgment);

            verify(messageRepository).bulkDecrementUnreadCountAfterCursor(CHANNEL_ID, LAST_READ);
            verify(metadataRepository).deleteByChannelIdAndUserId(CHANNEL_ID, USER_ID);
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("lastReadAt null (한 번도 읽지 않음) → 전체 메시지 감소")
        void shouldDecrementAllWhenLastReadAtIsNull() throws Exception {
            MemberLeftKafkaEvent event = new MemberLeftKafkaEvent(USER_ID, CHANNEL_ID, null);
            when(objectMapper.readValue(anyString(), eq(MemberLeftKafkaEvent.class))).thenReturn(event);
            when(messageRepository.bulkDecrementUnreadCountAfterCursor(CHANNEL_ID, null)).thenReturn(20);

            consumer.consume(record("{}"), acknowledgment);

            verify(messageRepository).bulkDecrementUnreadCountAfterCursor(CHANNEL_ID, null);
            verify(metadataRepository).deleteByChannelIdAndUserId(CHANNEL_ID, USER_ID);
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("업데이트 결과 0건이어도 메타데이터 삭제 후 ACK")
        void shouldDeleteMetadataEvenWhenZeroMessagesUpdated() throws Exception {
            MemberLeftKafkaEvent event = new MemberLeftKafkaEvent(USER_ID, CHANNEL_ID, LAST_READ);
            when(objectMapper.readValue(anyString(), eq(MemberLeftKafkaEvent.class))).thenReturn(event);
            when(messageRepository.bulkDecrementUnreadCountAfterCursor(any(), any())).thenReturn(0);

            consumer.consume(record("{}"), acknowledgment);

            verify(metadataRepository).deleteByChannelIdAndUserId(CHANNEL_ID, USER_ID);
            verify(acknowledgment).acknowledge();
        }
    }

    // ──────────────────────────────────────────────────
    // Invalid Event
    // ──────────────────────────────────────────────────
    @Nested
    @DisplayName("유효하지 않은 이벤트")
    class InvalidEvent {

        @Test
        @DisplayName("channelId null → 스킵(ACK), DB 호출 없음")
        void shouldSkipWhenChannelIdNull() throws Exception {
            when(objectMapper.readValue(anyString(), eq(MemberLeftKafkaEvent.class)))
                    .thenReturn(new MemberLeftKafkaEvent(USER_ID, null, LAST_READ));

            consumer.consume(record("{}"), acknowledgment);

            verifyNoInteractions(messageRepository, metadataRepository);
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("userId null → 스킵(ACK), DB 호출 없음")
        void shouldSkipWhenUserIdNull() throws Exception {
            when(objectMapper.readValue(anyString(), eq(MemberLeftKafkaEvent.class)))
                    .thenReturn(new MemberLeftKafkaEvent(null, CHANNEL_ID, LAST_READ));

            consumer.consume(record("{}"), acknowledgment);

            verifyNoInteractions(messageRepository, metadataRepository);
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("역직렬화 null 반환 → 스킵(ACK)")
        void shouldSkipWhenDeserializationReturnsNull() throws Exception {
            when(objectMapper.readValue(anyString(), eq(MemberLeftKafkaEvent.class))).thenReturn(null);

            consumer.consume(record("null"), acknowledgment);

            verifyNoInteractions(messageRepository, metadataRepository);
            verify(acknowledgment).acknowledge();
        }
    }

    // ──────────────────────────────────────────────────
    // Exception Handling
    // ──────────────────────────────────────────────────
    @Nested
    @DisplayName("예외 처리")
    class ExceptionHandling {

        @Test
        @DisplayName("JSON 파싱 실패 → 예외 전파 없이 ACK")
        void shouldNotPropagateJsonException() throws Exception {
            when(objectMapper.readValue(anyString(), eq(MemberLeftKafkaEvent.class)))
                    .thenThrow(new com.fasterxml.jackson.core.JsonParseException(null, "bad"));

            assertThatNoException().isThrownBy(() -> consumer.consume(record("bad"), acknowledgment));
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("DB 예외 발생 → 예외 전파 없이 ACK")
        void shouldNotPropagateDbException() throws Exception {
            MemberLeftKafkaEvent event = new MemberLeftKafkaEvent(USER_ID, CHANNEL_ID, LAST_READ);
            when(objectMapper.readValue(anyString(), eq(MemberLeftKafkaEvent.class))).thenReturn(event);
            when(messageRepository.bulkDecrementUnreadCountAfterCursor(any(), any()))
                    .thenThrow(new RuntimeException("DB down"));

            assertThatNoException().isThrownBy(() -> consumer.consume(record("{}"), acknowledgment));
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("bulkDecrement 성공 후 deleteMetadata 예외 → 예외 전파 없이 ACK")
        void shouldNotPropagateDeleteMetadataException() throws Exception {
            MemberLeftKafkaEvent event = new MemberLeftKafkaEvent(USER_ID, CHANNEL_ID, LAST_READ);
            when(objectMapper.readValue(anyString(), eq(MemberLeftKafkaEvent.class))).thenReturn(event);
            when(messageRepository.bulkDecrementUnreadCountAfterCursor(any(), any())).thenReturn(3);
            doThrow(new RuntimeException("constraint error"))
                    .when(metadataRepository).deleteByChannelIdAndUserId(CHANNEL_ID, USER_ID);

            assertThatNoException().isThrownBy(() -> consumer.consume(record("{}"), acknowledgment));
            verify(acknowledgment).acknowledge();
        }
    }
}
