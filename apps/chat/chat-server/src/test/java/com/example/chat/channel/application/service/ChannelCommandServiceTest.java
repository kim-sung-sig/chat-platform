package com.example.chat.channel.application.service;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.chat.auth.core.util.SecurityUtils;
import com.example.chat.common.core.enums.ChannelType;
import com.example.chat.shared.exception.ChatException;
import com.example.chat.shared.exception.ResourceNotFoundException;
import com.example.chat.message.infrastructure.kafka.KafkaMessageProducer;
import com.example.chat.storage.domain.entity.ChatChannelEntity;
import com.example.chat.storage.domain.entity.ChatChannelMetadataEntity;
import com.example.chat.storage.domain.repository.JpaChannelMemberRepository;
import com.example.chat.storage.domain.repository.JpaChannelMetadataRepository;
import com.example.chat.storage.domain.repository.JpaChannelRepository;
import com.example.chat.storage.domain.repository.JpaUserRepository;

/**
 * [단위 테스트] ChannelCommandService.removeMember()
 *
 * 검증 범위:
 * - 정상 퇴장 (소유자가 타 멤버 제거): deleteByChannelIdAndUserId + publishMemberLeft(lastReadAt)
 * - 자기 자신 퇴장: 동일한 흐름
 * - lastReadAt null (한 번도 읽지 않음): publishMemberLeft(null)
 * - 소유자 스스로 퇴장 시도: ChatException (DOMAIN_RULE_VIOLATION)
 * - 권한 없는 사용자 제거 시도: ChatException
 * - 채널 없음: ResourceNotFoundException
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ChannelCommandService.removeMember 단위 테스트")
class ChannelCommandServiceTest {

    @Mock JpaChannelRepository channelRepository;
    @Mock JpaChannelMemberRepository channelMemberRepository;
    @Mock JpaUserRepository userRepository;
    @Mock JpaChannelMetadataRepository channelMetadataRepository;
    @Mock KafkaMessageProducer kafkaMessageProducer;

    @InjectMocks ChannelCommandService service;

    private MockedStatic<SecurityUtils> mockedSecurity;

    private static final String OWNER_ID   = "owner-001";
    private static final String MEMBER_ID  = "member-002";
    private static final String CHANNEL_ID = "ch-001";
    private static final Instant LAST_READ = Instant.parse("2026-03-10T09:00:00Z");

    @BeforeEach
    void openMockStatic() {
        mockedSecurity = Mockito.mockStatic(SecurityUtils.class);
    }

    @AfterEach
    void closeMockStatic() {
        mockedSecurity.close();
    }

    private ChatChannelEntity activeChannel(String ownerId) {
        return ChatChannelEntity.builder()
                .id(CHANNEL_ID)
                .name("test-ch")
                .channelType(ChannelType.GROUP)
                .ownerId(ownerId)
                .active(true)
                .build();
    }

    private ChatChannelMetadataEntity metadataWith(Instant lastReadAt) {
        return ChatChannelMetadataEntity.builder()
                .id("meta-001")
                .channelId(CHANNEL_ID)
                .userId(MEMBER_ID)
                .lastReadAt(lastReadAt)
                .build();
    }

    // ──────────────────────────────────────────────────
    // Happy Path — 소유자가 멤버 제거
    // ──────────────────────────────────────────────────
    @Nested
    @DisplayName("정상 퇴장 처리")
    class HappyPath {

        @BeforeEach
        void setUp() {
            mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(Optional.of(OWNER_ID));
            when(channelRepository.findById(CHANNEL_ID)).thenReturn(Optional.of(activeChannel(OWNER_ID)));
        }

        @Test
        @DisplayName("channelMemberRepository.deleteByChannelIdAndUserId 호출")
        void shouldDeleteMember() {
            when(channelMetadataRepository.findByChannelIdAndUserId(CHANNEL_ID, MEMBER_ID))
                    .thenReturn(Optional.of(metadataWith(LAST_READ)));

            service.removeMember(CHANNEL_ID, MEMBER_ID);

            verify(channelMemberRepository).deleteByChannelIdAndUserId(CHANNEL_ID, MEMBER_ID);
        }

        @Test
        @DisplayName("publishMemberLeft 에 lastReadAt 커서 포함")
        void shouldPublishMemberLeftWithLastReadAt() {
            when(channelMetadataRepository.findByChannelIdAndUserId(CHANNEL_ID, MEMBER_ID))
                    .thenReturn(Optional.of(metadataWith(LAST_READ)));

            service.removeMember(CHANNEL_ID, MEMBER_ID);

            verify(kafkaMessageProducer).publishMemberLeft(MEMBER_ID, CHANNEL_ID, LAST_READ);
        }

        @Test
        @DisplayName("metadata 없으면 lastReadAt=null 로 publishMemberLeft 호출")
        void shouldPublishMemberLeftWithNullWhenNoMetadata() {
            when(channelMetadataRepository.findByChannelIdAndUserId(CHANNEL_ID, MEMBER_ID))
                    .thenReturn(Optional.empty());

            service.removeMember(CHANNEL_ID, MEMBER_ID);

            verify(kafkaMessageProducer).publishMemberLeft(MEMBER_ID, CHANNEL_ID, null);
        }

        @Test
        @DisplayName("metadata lastReadAt null 이면 null 그대로 전달")
        void shouldPublishMemberLeftWithNullLastReadAt() {
            when(channelMetadataRepository.findByChannelIdAndUserId(CHANNEL_ID, MEMBER_ID))
                    .thenReturn(Optional.of(metadataWith(null)));

            service.removeMember(CHANNEL_ID, MEMBER_ID);

            verify(kafkaMessageProducer).publishMemberLeft(MEMBER_ID, CHANNEL_ID, null);
        }
    }

    // ──────────────────────────────────────────────────
    // Self-leave
    // ──────────────────────────────────────────────────
    @Nested
    @DisplayName("자기 자신 퇴장")
    class SelfLeave {

        @Test
        @DisplayName("자기 자신 퇴장 정상 처리")
        void shouldAllowSelfLeave() {
            mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(Optional.of(MEMBER_ID));
            when(channelRepository.findById(CHANNEL_ID)).thenReturn(Optional.of(activeChannel(OWNER_ID)));
            when(channelMetadataRepository.findByChannelIdAndUserId(CHANNEL_ID, MEMBER_ID))
                    .thenReturn(Optional.of(metadataWith(LAST_READ)));

            service.removeMember(CHANNEL_ID, MEMBER_ID);

            verify(channelMemberRepository).deleteByChannelIdAndUserId(CHANNEL_ID, MEMBER_ID);
            verify(kafkaMessageProducer).publishMemberLeft(MEMBER_ID, CHANNEL_ID, LAST_READ);
        }
    }

    // ──────────────────────────────────────────────────
    // Constraint Violations
    // ──────────────────────────────────────────────────
    @Nested
    @DisplayName("제약 조건 위반")
    class Constraints {

        @Test
        @DisplayName("소유자 자신을 제거하면 ChatException(DOMAIN_RULE_VIOLATION)")
        void shouldThrowWhenOwnerTriesToRemoveSelf() {
            mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(Optional.of(OWNER_ID));
            when(channelRepository.findById(CHANNEL_ID)).thenReturn(Optional.of(activeChannel(OWNER_ID)));

            assertThatThrownBy(() -> service.removeMember(CHANNEL_ID, OWNER_ID))
                    .isInstanceOf(ChatException.class);

            verify(channelMemberRepository, never()).deleteByChannelIdAndUserId(any(), any());
            verify(kafkaMessageProducer, never()).publishMemberLeft(any(), any(), any());
        }

        @Test
        @DisplayName("권한 없는 사용자가 타 멤버 제거 시 ChatException")
        void shouldThrowWhenUnauthorizedUserTriesToRemoveMember() {
            String unauthorizedUser = "stranger-999";
            mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(Optional.of(unauthorizedUser));
            when(channelRepository.findById(CHANNEL_ID)).thenReturn(Optional.of(activeChannel(OWNER_ID)));

            assertThatThrownBy(() -> service.removeMember(CHANNEL_ID, MEMBER_ID))
                    .isInstanceOf(ChatException.class);

            verify(channelMemberRepository, never()).deleteByChannelIdAndUserId(any(), any());
            verify(kafkaMessageProducer, never()).publishMemberLeft(any(), any(), any());
        }

        @Test
        @DisplayName("채널 없으면 ResourceNotFoundException")
        void shouldThrowWhenChannelNotFound() {
            mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(Optional.of(OWNER_ID));
            when(channelRepository.findById(CHANNEL_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.removeMember(CHANNEL_ID, MEMBER_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ──────────────────────────────────────────────────
    // Order guarantee: delete BEFORE publishMemberLeft
    // ──────────────────────────────────────────────────
    @Nested
    @DisplayName("퇴장 순서 보장")
    class OrderGuarantee {

        @Test
        @DisplayName("멤버 삭제 후 Kafka 발행 — 순서 검증")
        void shouldDeleteMemberBeforePublishingKafkaEvent() {
            mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(Optional.of(OWNER_ID));
            when(channelRepository.findById(CHANNEL_ID)).thenReturn(Optional.of(activeChannel(OWNER_ID)));
            when(channelMetadataRepository.findByChannelIdAndUserId(CHANNEL_ID, MEMBER_ID))
                    .thenReturn(Optional.of(metadataWith(LAST_READ)));

            var inOrder = inOrder(channelMemberRepository, kafkaMessageProducer);

            service.removeMember(CHANNEL_ID, MEMBER_ID);

            inOrder.verify(channelMemberRepository).deleteByChannelIdAndUserId(CHANNEL_ID, MEMBER_ID);
            inOrder.verify(kafkaMessageProducer).publishMemberLeft(MEMBER_ID, CHANNEL_ID, LAST_READ);
        }
    }
}
