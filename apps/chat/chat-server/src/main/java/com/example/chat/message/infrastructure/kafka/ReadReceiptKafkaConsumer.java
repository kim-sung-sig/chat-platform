package com.example.chat.message.infrastructure.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.chat.storage.domain.repository.JpaMessageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 읽음 처리 Kafka Consumer
 *
 * 토픽: read-receipt-events
 * 역할: chat_messages.unread_count 비동기 일괄 감소
 *
 * 설계 결정 사항:
 * - markAsRead() API 응답은 즉시 반환 (metadata 갱신 + Redis 브로드캐스트만 동기)
 * - message.unread_count 갱신은 여기서 비동기 처리 (대규모 채널에서 응답 지연 방지)
 * - 동시성: channelId를 Kafka 파티션 키로 사용 → 같은 채널 이벤트 순서 보장
 * - 멱등성: CASE WHEN unread_count > 0 THEN - 1 ELSE 0 → 중복 소비 안전
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReadReceiptKafkaConsumer {

    private static final String TOPIC = "read-receipt-events";
    private static final String GROUP = "chat-server-group";

    private final JpaMessageRepository messageRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = TOPIC, groupId = GROUP)
    public void consume(ConsumerRecord<String, String> record, Acknowledgment ack) {
        try {
            ReadReceiptKafkaEvent event = objectMapper.readValue(record.value(), ReadReceiptKafkaEvent.class);
            if (event == null || event.channelId() == null || event.lastReadCreatedAt() == null) {
                log.warn("Invalid read-receipt event: {}", record.value());
                ack.acknowledge();
                return;
            }
            processEvent(event);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Failed to process read-receipt event: {}", record.value(), e);
            // ACK: 파싱/DB 실패 시 무한 루프 방지
            // 프로덕션 환경에서는 DLQ(read-receipt-events.DLT)로 이동 권장
            ack.acknowledge();
        }
    }

    /**
     * DB 작업을 단일 트랜잭션으로 묶음
     * - processEvent() 내에서 RuntimeException 발생 시 @Transactional이 롤백 처리
     * - consume()의 catch가 예외를 삼키더라도 이 메서드 경계에서 롤백 완료됨
     * - TransactionRoutingDataSource: readOnly=false → SOURCE(Write DB) 자동 라우팅
     */
    @Transactional
    public void processEvent(ReadReceiptKafkaEvent event) {
        // 커서(lastReadCreatedAt) 이전 메시지들의 unread_count 일괄 -1 감소
        int updated = messageRepository.bulkDecrementUnreadCountBeforeCursor(
                event.channelId(), event.lastReadCreatedAt());

        log.info("ReadReceipt processed: userId={}, channelId={}, updatedMessages={}",
                event.userId(), event.channelId(), updated);
    }
}
