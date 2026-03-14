package com.example.chat.channel.infrastructure.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.chat.storage.domain.repository.JpaChannelMetadataRepository;
import com.example.chat.storage.domain.repository.JpaMessageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 채널 멤버 퇴장 Kafka Consumer
 *
 * 토픽: member-left-events
 * 역할: 퇴장 멤버가 읽지 않은 메시지들의 unread_count 일괄 -1 감소
 *
 * 처리 흐름:
 *  1. 퇴장한 사용자의 lastReadAt 기준으로 미읽음 메시지 조회
 *  2. chat_messages.unread_count 일괄 감소 (퇴장자는 영원히 읽지 않음)
 *  3. 해당 사용자의 chat_channel_metadata 삭제
 *
 * 大規模 채널 고려 사항:
 * - Kafka 비동기 처리로 removeMember() API 응답 블로킹 없음
 * - channelId 파티션 키로 순서 보장 (같은 채널의 연속 퇴장도 안전)
 * - 멱등성: CASE WHEN unread_count > 0 THEN - 1 ELSE 0 → 중복 소비 안전
 *
 * 트랜잭션 설계:
 * - processEvent()를 별도 @Transactional 메서드로 분리
 * - consume()의 try-catch가 예외를 삼켜도 processEvent() 내부에서 발생한
 *   RuntimeException은 @Transactional 경계에서 롤백됨 → 부분 커밋 방지
 * - bulkDecrement 성공 + deleteMetadata 실패 시 전체 롤백 보장
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MemberLeftKafkaConsumer {

    private static final String TOPIC = "member-left-events";
    private static final String GROUP = "chat-server-group";

    private final JpaMessageRepository messageRepository;
    private final JpaChannelMetadataRepository metadataRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = TOPIC, groupId = GROUP)
    public void consume(ConsumerRecord<String, String> record, Acknowledgment ack) {
        try {
            MemberLeftKafkaEvent event = objectMapper.readValue(record.value(), MemberLeftKafkaEvent.class);
            if (event == null || event.channelId() == null || event.userId() == null) {
                log.warn("Invalid member-left event: {}", record.value());
                ack.acknowledge();
                return;
            }
            processEvent(event);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Failed to process member-left event: {}", record.value(), e);
            // ACK: 파싱/DB 실패 시 무한 재처리 방지
            // 프로덕션 환경에서는 DLQ(member-left-events.DLT)로 이동 권장
            ack.acknowledge();
        }
    }

    /**
     * DB 작업을 단일 트랜잭션으로 묶어 원자성 보장
     * - bulkDecrementUnreadCountAfterCursor 성공 후 deleteByChannelIdAndUserId 실패 시 전체 롤백
     * - TransactionRoutingDataSource: readOnly=false → SOURCE(Write DB) 자동 라우팅
     */
    @Transactional
    public void processEvent(MemberLeftKafkaEvent event) {
        // lastReadAt 커서 이후(미읽음) 메시지의 unread_count 일괄 -1 감소
        // lastReadAt == null: 한 번도 읽지 않음 → 채널 전체 메시지 감소
        int updated = messageRepository.bulkDecrementUnreadCountAfterCursor(
                event.channelId(), event.lastReadAt());

        // 퇴장 사용자의 채널 메타데이터 삭제
        metadataRepository.deleteByChannelIdAndUserId(event.channelId(), event.userId());

        log.info("MemberLeft processed: userId={}, channelId={}, updatedMessages={}",
                event.userId(), event.channelId(), updated);
    }
}
