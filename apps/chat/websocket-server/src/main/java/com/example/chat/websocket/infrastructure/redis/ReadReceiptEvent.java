package com.example.chat.websocket.infrastructure.redis;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 읽음 처리 완료 이벤트 DTO
 *
 * Redis 채널: chat:read:event:{channelId} 에서 수신
 * 발행자: chat-server (ReadReceiptEventPublisher)
 * 역할: WebSocket으로 채널 내 모든 접속자에게 읽음 상태 변경 전파
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReadReceiptEvent {
    /** 이벤트 타입: READ_RECEIPT */
    private String eventType;
    /** 읽음 처리한 사용자 ID */
    private String userId;
    /** 채널 ID */
    private String channelId;
    /** 마지막으로 읽은 메시지 ID (이 메시지 이하는 모두 읽음 처리됨) */
    private String lastReadMessageId;
    /** 읽음 처리 시각 */
    private Instant readAt;
}
