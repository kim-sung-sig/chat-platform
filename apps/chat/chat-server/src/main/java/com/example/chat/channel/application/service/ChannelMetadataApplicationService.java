package com.example.chat.channel.application.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.chat.channel.application.dto.response.ChannelMetadataResponse;
import com.example.chat.channel.infrastructure.redis.ReadReceiptEventPublisher;
import com.example.chat.common.core.exception.ChatErrorCode;
import com.example.chat.exception.ChatException;
import com.example.chat.exception.ResourceNotFoundException;
import com.example.chat.message.infrastructure.kafka.KafkaMessageProducer;
import com.example.chat.message.infrastructure.kafka.ReadReceiptKafkaEvent;
import com.example.chat.storage.entity.ChatChannelMetadataEntity;
import com.example.chat.storage.repository.JpaChannelMemberRepository;
import com.example.chat.storage.repository.JpaChannelMetadataRepository;
import com.example.chat.storage.repository.JpaChannelRepository;
import com.example.chat.storage.repository.JpaMessageRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * 채팅방 메타데이터 Application Service
 *
 * 트랜잭션 정책:
 * - 클래스 기본: readOnly=true (조회 최적화)
 * - 상태 변경 메서드: @Transactional 개별 오버라이드
 * - getOrCreateMetadata: 조회+생성 혼재 → 쓰기 트랜잭션
 */
@Service
@Transactional(readOnly = true)
@Slf4j
public class ChannelMetadataApplicationService {

    private final JpaChannelMetadataRepository metadataRepository;
    private final JpaChannelRepository channelRepository;
    private final JpaChannelMemberRepository channelMemberRepository;
    private final ReadReceiptEventPublisher readReceiptEventPublisher;
    private final KafkaMessageProducer kafkaMessageProducer;
    private final JpaMessageRepository messageRepository;

    public ChannelMetadataApplicationService(
            JpaChannelMetadataRepository metadataRepository,
            JpaChannelRepository channelRepository,
            JpaChannelMemberRepository channelMemberRepository,
            ReadReceiptEventPublisher readReceiptEventPublisher,
            KafkaMessageProducer kafkaMessageProducer,
            JpaMessageRepository messageRepository) {
        this.metadataRepository = metadataRepository;
        this.channelRepository = channelRepository;
        this.channelMemberRepository = channelMemberRepository;
        this.readReceiptEventPublisher = readReceiptEventPublisher;
        this.kafkaMessageProducer = kafkaMessageProducer;
        this.messageRepository = messageRepository;
    }

    /** 조회 또는 신규 생성 - 쓰기 트랜잭션 */
    @Transactional
    public ChannelMetadataResponse getOrCreateMetadata(String userId, String channelId) {
        log.debug("Getting or creating metadata: userId={}, channelId={}", userId, channelId);

        channelRepository.findById(channelId)
                .orElseThrow(() -> new ResourceNotFoundException(ChatErrorCode.CHANNEL_NOT_FOUND));

        if (!channelMemberRepository.existsByChannelIdAndUserId(channelId, userId)) {
            throw new ChatException(ChatErrorCode.CHANNEL_NOT_MEMBER);
        }

        ChatChannelMetadataEntity metadata = metadataRepository
                .findByChannelIdAndUserId(channelId, userId)
                .orElseGet(() -> metadataRepository.save(buildMetadata(channelId, userId)));

        return ChannelMetadataResponse.fromEntity(metadata);
    }

    /** 읽음 처리 - 쓰기 트랜잭션 */
    @Transactional
    public ChannelMetadataResponse markAsRead(String userId, String channelId, String messageId) {
        log.info("Marking as read: userId={}, channelId={}, messageId={}", userId, channelId, messageId);
        ChatChannelMetadataEntity metadata = findMetadata(userId, channelId);
        metadata.markAsRead(messageId);
        ChannelMetadataResponse response = ChannelMetadataResponse.fromEntity(metadataRepository.save(metadata));

        // 1) 실시간: Redis Pub/Sub → WebSocket read receipt 브로드캐스트 (즉시)
        readReceiptEventPublisher.publish(userId, channelId, messageId);

        // 2) 비동기: Kafka → message.unread_count 배치 감소 (응답 지연 없음)
        publishReadReceiptKafkaEvent(userId, channelId, messageId);

        return response;
    }

    /**
     * 읽음 처리 Kafka 이벤트 발행
     * 메시지의 createdAt을 조회해 커서로 사용
     */
    private void publishReadReceiptKafkaEvent(String userId, String channelId, String messageId) {
        try {
            messageRepository.findById(messageId).ifPresent(msg -> {
                kafkaMessageProducer.publishReadReceipt(
                        new ReadReceiptKafkaEvent(userId, channelId, messageId, msg.getCreatedAt()));
                log.debug("ReadReceipt Kafka event published: userId={}, channelId={}, cursor={}",
                        userId, channelId, msg.getCreatedAt());
            });
        } catch (Exception e) {
            // Kafka 발행 실패는 non-critical (미읽음 카운터 일시적 부정확)
            log.error("Failed to publish read-receipt Kafka event: userId={}, channelId={}", userId, channelId, e);
        }
    }

    /** 미읽음 카운트 증가 - 쓰기 트랜잭션 */
    @Transactional
    public void incrementUnreadCount(String userId, String channelId) {
        log.debug("Incrementing unread count: userId={}, channelId={}", userId, channelId);
        ChatChannelMetadataEntity metadata = metadataRepository
                .findByChannelIdAndUserId(channelId, userId)
                .orElseGet(() -> buildMetadata(channelId, userId));
        metadata.incrementUnread();
        metadataRepository.save(metadata);
    }

    /** 알림 토글 - 쓰기 트랜잭션 */
    @Transactional
    public ChannelMetadataResponse toggleNotification(String userId, String channelId) {
        log.info("Toggling notification: userId={}, channelId={}", userId, channelId);
        ChatChannelMetadataEntity metadata = findMetadata(userId, channelId);
        metadata.toggleNotification();
        return ChannelMetadataResponse.fromEntity(metadataRepository.save(metadata));
    }

    /** 즐겨찾기 토글 - 쓰기 트랜잭션 */
    @Transactional
    public ChannelMetadataResponse toggleFavorite(String userId, String channelId) {
        log.info("Toggling favorite: userId={}, channelId={}", userId, channelId);
        ChatChannelMetadataEntity metadata = findMetadata(userId, channelId);
        metadata.toggleFavorite();
        return ChannelMetadataResponse.fromEntity(metadataRepository.save(metadata));
    }

    /** 상단 고정 토글 - 쓰기 트랜잭션 */
    @Transactional
    public ChannelMetadataResponse togglePinned(String userId, String channelId) {
        log.info("Toggling pinned: userId={}, channelId={}", userId, channelId);
        ChatChannelMetadataEntity metadata = findMetadata(userId, channelId);
        metadata.togglePin();
        return ChannelMetadataResponse.fromEntity(metadataRepository.save(metadata));
    }

    public List<ChannelMetadataResponse> getFavorites(String userId) {
        return metadataRepository.findFavoritesByUserId(userId).stream()
                .map(ChannelMetadataResponse::fromEntity).collect(Collectors.toList());
    }

    public List<ChannelMetadataResponse> getPinned(String userId) {
        return metadataRepository.findPinnedByUserId(userId).stream()
                .map(ChannelMetadataResponse::fromEntity).collect(Collectors.toList());
    }

    public List<ChannelMetadataResponse> getWithUnread(String userId) {
        return metadataRepository.findWithUnreadByUserId(userId).stream()
                .map(ChannelMetadataResponse::fromEntity).collect(Collectors.toList());
    }

    // =============================================
    // Private Helpers
    // =============================================

    private ChatChannelMetadataEntity findMetadata(String userId, String channelId) {
        return metadataRepository.findByChannelIdAndUserId(channelId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(ChatErrorCode.CHANNEL_NOT_FOUND));
    }

    private ChatChannelMetadataEntity buildMetadata(String channelId, String userId) {
        return ChatChannelMetadataEntity.builder()
                .id(UUID.randomUUID().toString())
                .channelId(channelId)
                .userId(userId)
                .build();
    }
}
