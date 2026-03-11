package com.example.chat.message.infrastructure.kafka;

import java.time.Instant;

/**
 * 읽음 처리 Kafka 이벤트
 *
 * 토픽: read-receipt-events
 * 발행자: ChannelMetadataApplicationService.markAsRead()
 * 소비자: ReadReceiptKafkaConsumer
 *
 * 목적: chat_messages.unread_count 비동기 일괄 감소
 *       markAsRead 처리와 message unread_count 갱신을 분리하여 응답 지연 최소화
 *
 * @param userId              읽음 처리한 사용자 ID (발신자 제외)
 * @param channelId           채널 ID
 * @param lastReadMessageId   마지막으로 읽은 메시지 ID
 * @param lastReadCreatedAt   마지막 읽음 메시지의 created_at (커서 기준)
 */
public record ReadReceiptKafkaEvent(
        String userId,
        String channelId,
        String lastReadMessageId,
        Instant lastReadCreatedAt) {
}
