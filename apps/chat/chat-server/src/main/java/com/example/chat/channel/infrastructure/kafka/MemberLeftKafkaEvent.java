package com.example.chat.channel.infrastructure.kafka;

import java.time.Instant;

/**
 * 채널 멤버 퇴장 Kafka 이벤트
 *
 * 토픽: member-left-events
 * 발행자: ChannelCommandService.removeMember()
 * 소비자: MemberLeftKafkaConsumer
 *
 * 목적: 멤버 퇴장 시 해당 사용자가 읽지 않은 메시지들의 unread_count 일괄 감소
 *       퇴장한 멤버는 더 이상 읽을 수 없으므로 미읽음 카운터에서 제거
 *
 * @param userId          퇴장한 사용자 ID
 * @param channelId       채널 ID
 * @param lastReadAt      마지막 읽음 기준 시각 (null이면 한 번도 읽지 않음 → 전체 감소)
 */
public record MemberLeftKafkaEvent(
        String userId,
        String channelId,
        Instant lastReadAt) {
}
