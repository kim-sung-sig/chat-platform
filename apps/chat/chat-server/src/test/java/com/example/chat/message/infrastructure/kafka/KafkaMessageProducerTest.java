package com.example.chat.message.infrastructure.kafka;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatNoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * [단위 테스트] KafkaMessageProducer
 *
 * 검증 범위:
 * - publishReadReceipt: READ_RECEIPT_TOPIC / channelId 파티션키 / JSON 직렬화
 * - publishMemberLeft:  MEMBER_LEFT_TOPIC  / channelId 파티션키 / JSON 직렬화
 * - publishNotification: NOTIFICATION_TOPIC / JSON 직렬화
 * - 직렬화 예외 → 예외 전파 없음 (fire-and-forget 설계)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("KafkaMessageProducer 단위 테스트")
class KafkaMessageProducerTest {

    @Mock KafkaTemplate<String, String> kafkaTemplate;
    @Mock ObjectMapper objectMapper;

    @InjectMocks KafkaMessageProducer producer;

    private static final String CHANNEL_ID  = "ch-001";
    private static final String USER_ID     = "u-001";
    private static final String MESSAGE_ID  = "msg-001";
    private static final Instant CURSOR     = Instant.parse("2026-03-12T10:00:00Z");
    private static final String SERIALIZED  = "{\"serialized\":\"value\"}";

    // ──────────────────────────────────────────────────
    // publishReadReceipt
    // ──────────────────────────────────────────────────
    @Nested
    @DisplayName("publishReadReceipt")
    class PublishReadReceipt {

        @Test
        @DisplayName("read-receipt-events 토픽에 channelId 파티션키로 발행")
        void shouldPublishToReadReceiptTopicWithChannelIdKey() throws Exception {
            ReadReceiptKafkaEvent event = new ReadReceiptKafkaEvent(USER_ID, CHANNEL_ID, MESSAGE_ID, CURSOR);
            when(objectMapper.writeValueAsString(event)).thenReturn(SERIALIZED);

            producer.publishReadReceipt(event);

            verify(kafkaTemplate).send("read-receipt-events", CHANNEL_ID, SERIALIZED);
        }

        @Test
        @DisplayName("직렬화 예외 발생 시 예외 전파 없음")
        void shouldSilentlyFailOnSerializationError() throws Exception {
            ReadReceiptKafkaEvent event = new ReadReceiptKafkaEvent(USER_ID, CHANNEL_ID, MESSAGE_ID, CURSOR);
            when(objectMapper.writeValueAsString(any())).thenThrow(new com.fasterxml.jackson.core.JsonProcessingException("err"){});

            assertThatNoException().isThrownBy(() -> producer.publishReadReceipt(event));
            verifyNoInteractions(kafkaTemplate);
        }
    }

    // ──────────────────────────────────────────────────
    // publishMemberLeft
    // ──────────────────────────────────────────────────
    @Nested
    @DisplayName("publishMemberLeft")
    class PublishMemberLeft {

        @Test
        @DisplayName("member-left-events 토픽에 channelId 파티션키로 발행")
        void shouldPublishToMemberLeftTopicWithChannelIdKey() throws Exception {
            when(objectMapper.writeValueAsString(any())).thenReturn(SERIALIZED);

            producer.publishMemberLeft(USER_ID, CHANNEL_ID, CURSOR);

            verify(kafkaTemplate).send("member-left-events", CHANNEL_ID, SERIALIZED);
        }

        @Test
        @DisplayName("lastReadAt null 이어도 발행 성공")
        void shouldPublishWithNullLastReadAt() throws Exception {
            when(objectMapper.writeValueAsString(any())).thenReturn(SERIALIZED);

            producer.publishMemberLeft(USER_ID, CHANNEL_ID, null);

            verify(kafkaTemplate).send("member-left-events", CHANNEL_ID, SERIALIZED);
        }

        @Test
        @DisplayName("직렬화 예외 발생 시 예외 전파 없음")
        void shouldSilentlyFailOnSerializationError() throws Exception {
            when(objectMapper.writeValueAsString(any())).thenThrow(new com.fasterxml.jackson.core.JsonProcessingException("err"){});

            assertThatNoException().isThrownBy(() -> producer.publishMemberLeft(USER_ID, CHANNEL_ID, CURSOR));
            verifyNoInteractions(kafkaTemplate);
        }
    }

    // ──────────────────────────────────────────────────
    // publishNotification
    // ──────────────────────────────────────────────────
    @Nested
    @DisplayName("publishNotification")
    class PublishNotification {

        @Test
        @DisplayName("notification-events 토픽에 발행")
        void shouldPublishToNotificationTopic() throws Exception {
            when(objectMapper.writeValueAsString(any())).thenReturn(SERIALIZED);

            producer.publishNotification(USER_ID, "title", "content", "CHAT_MESSAGE");

            verify(kafkaTemplate).send("notification-events", SERIALIZED);
        }

        @Test
        @DisplayName("직렬화 예외 발생 시 예외 전파 없음")
        void shouldSilentlyFailOnSerializationError() throws Exception {
            when(objectMapper.writeValueAsString(any())).thenThrow(new com.fasterxml.jackson.core.JsonProcessingException("err"){});

            assertThatNoException().isThrownBy(
                    () -> producer.publishNotification(USER_ID, "t", "c", "CHAT_MESSAGE"));
            verifyNoInteractions(kafkaTemplate);
        }
    }
}
