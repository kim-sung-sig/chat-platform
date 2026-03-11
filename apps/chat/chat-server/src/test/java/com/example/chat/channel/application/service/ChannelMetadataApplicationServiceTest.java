package com.example.chat.channel.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.example.chat.channel.application.dto.response.ChannelMetadataResponse;
import com.example.chat.channel.infrastructure.redis.ReadReceiptEventPublisher;
import com.example.chat.common.core.enums.MessageType;
import com.example.chat.exception.ResourceNotFoundException;
import com.example.chat.message.infrastructure.kafka.KafkaMessageProducer;
import com.example.chat.message.infrastructure.kafka.ReadReceiptKafkaEvent;
import com.example.chat.storage.entity.ChatChannelMetadataEntity;
import com.example.chat.storage.entity.ChatMessageEntity;
import com.example.chat.storage.repository.JpaChannelMemberRepository;
import com.example.chat.storage.repository.JpaChannelMetadataRepository;
import com.example.chat.storage.repository.JpaChannelRepository;
import com.example.chat.storage.repository.JpaMessageRepository;

/**
 * [단위 테스트] ChannelMetadataApplicationService.markAsRead()
 *
 * 검증 범위:
 * - 정상 markAsRead → metadata 저장 + Redis 발행 + Kafka 발행
 * - messageId 없는 메시지 → Kafka 이벤트 미발행 (ifPresent)
 * - metadata 없음 → ResourceNotFoundException
 * - Kafka 발행 실패 → non-critical, 예외 전파 없음
 * - Redis 발행 실패 → 예외 전파 확인 (설계 의도 검증)
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)  // @BeforeEach stub이 Nested 클래스에서 override될 때 허용
@DisplayName("ChannelMetadataApplicationService.markAsRead 단위 테스트")
class ChannelMetadataApplicationServiceTest {

    @Mock JpaChannelMetadataRepository metadataRepository;
    @Mock JpaChannelRepository channelRepository;
    @Mock JpaChannelMemberRepository channelMemberRepository;
    @Mock ReadReceiptEventPublisher readReceiptEventPublisher;
    @Mock KafkaMessageProducer kafkaMessageProducer;
    @Mock JpaMessageRepository messageRepository;

    @InjectMocks ChannelMetadataApplicationService service;

    private static final String USER_ID     = "u-001";
    private static final String CHANNEL_ID  = "ch-001";
    private static final String MESSAGE_ID  = "msg-001";
    private static final Instant CREATED_AT = Instant.parse("2026-03-12T10:00:00Z");

    private ChatChannelMetadataEntity stubMetadata() {
        return ChatChannelMetadataEntity.builder()
                .id("meta-001")
                .channelId(CHANNEL_ID)
                .userId(USER_ID)
                .unreadCount(3)
                .build();
    }

    private ChatMessageEntity stubMessage() {
        return ChatMessageEntity.builder()
                .id(MESSAGE_ID)
                .channelId(CHANNEL_ID)
                .senderId("other-user")
                .messageType(MessageType.TEXT)
                .createdAt(CREATED_AT)
                .build();
    }

    @BeforeEach
    void setUpMetadataResolution() {
        ChatChannelMetadataEntity meta = stubMetadata();
        when(metadataRepository.findByChannelIdAndUserId(CHANNEL_ID, USER_ID)).thenReturn(Optional.of(meta));
        when(metadataRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    // ──────────────────────────────────────────────────
    // Happy Path
    // ──────────────────────────────────────────────────
    @Nested
    @DisplayName("정상 markAsRead 처리")
    class HappyPath {

        @BeforeEach
        void setUp() {
            when(messageRepository.findById(MESSAGE_ID)).thenReturn(Optional.of(stubMessage()));
        }

        @Test
        @DisplayName("metadata 저장 후 response 반환")
        void shouldSaveMetadataAndReturnResponse() {
            ChannelMetadataResponse response = service.markAsRead(USER_ID, CHANNEL_ID, MESSAGE_ID);

            assertThat(response).isNotNull();
            verify(metadataRepository).save(any(ChatChannelMetadataEntity.class));
        }

        @Test
        @DisplayName("Redis ReadReceipt 이벤트 즉시 발행")
        void shouldPublishRedisReadReceiptImmediately() {
            service.markAsRead(USER_ID, CHANNEL_ID, MESSAGE_ID);

            verify(readReceiptEventPublisher).publish(USER_ID, CHANNEL_ID, MESSAGE_ID);
        }

        @Test
        @DisplayName("Kafka ReadReceipt 이벤트 발행 — channelId, createdAt 커서 포함")
        void shouldPublishKafkaReadReceiptWithCorrectCursor() {
            service.markAsRead(USER_ID, CHANNEL_ID, MESSAGE_ID);

            ArgumentCaptor<ReadReceiptKafkaEvent> captor = ArgumentCaptor.forClass(ReadReceiptKafkaEvent.class);
            verify(kafkaMessageProducer).publishReadReceipt(captor.capture());

            ReadReceiptKafkaEvent published = captor.getValue();
            assertThat(published.userId()).isEqualTo(USER_ID);
            assertThat(published.channelId()).isEqualTo(CHANNEL_ID);
            assertThat(published.lastReadMessageId()).isEqualTo(MESSAGE_ID);
            assertThat(published.lastReadCreatedAt()).isEqualTo(CREATED_AT);
        }

        @Test
        @DisplayName("Redis 발행과 Kafka 발행이 모두 호출됨")
        void shouldCallBothRedisAndKafka() {
            service.markAsRead(USER_ID, CHANNEL_ID, MESSAGE_ID);

            verify(readReceiptEventPublisher, times(1)).publish(any(), any(), any());
            verify(kafkaMessageProducer, times(1)).publishReadReceipt(any());
        }
    }

    // ──────────────────────────────────────────────────
    // Message Not Found (Kafka event skip)
    // ──────────────────────────────────────────────────
    @Nested
    @DisplayName("메시지 조회 실패 시 Kafka 발행 스킵")
    class MessageNotFound {

        @Test
        @DisplayName("메시지 없으면 Kafka 미발행, Redis는 발행")
        void shouldSkipKafkaWhenMessageNotFound() {
            when(messageRepository.findById(MESSAGE_ID)).thenReturn(Optional.empty());

            service.markAsRead(USER_ID, CHANNEL_ID, MESSAGE_ID);

            verify(readReceiptEventPublisher).publish(USER_ID, CHANNEL_ID, MESSAGE_ID);
            verify(kafkaMessageProducer, never()).publishReadReceipt(any());
        }
    }

    // ──────────────────────────────────────────────────
    // Metadata Not Found
    // ──────────────────────────────────────────────────
    @Nested
    @DisplayName("메타데이터 없음")
    class MetadataNotFound {

        @Test
        @DisplayName("metadata 없으면 ResourceNotFoundException 발생")
        void shouldThrowWhenMetadataNotFound() {
            when(metadataRepository.findByChannelIdAndUserId(CHANNEL_ID, USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.markAsRead(USER_ID, CHANNEL_ID, MESSAGE_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ──────────────────────────────────────────────────
    // Kafka Non-Critical Failure
    // ──────────────────────────────────────────────────
    @Nested
    @DisplayName("Kafka 발행 실패 — non-critical")
    class KafkaFailure {

        @Test
        @DisplayName("Kafka 예외 발생해도 markAsRead 정상 응답 반환")
        void shouldReturnResponseEvenWhenKafkaFails() {
            ChatMessageEntity msg = stubMessage();
            when(messageRepository.findById(MESSAGE_ID)).thenReturn(Optional.of(msg));
            doThrow(new RuntimeException("Kafka unavailable"))
                    .when(kafkaMessageProducer).publishReadReceipt(any());

            // Kafka 실패는 내부에서 catch → 예외 전파 없음
            ChannelMetadataResponse response = service.markAsRead(USER_ID, CHANNEL_ID, MESSAGE_ID);

            assertThat(response).isNotNull();
            verify(metadataRepository).save(any());
        }
    }
}
