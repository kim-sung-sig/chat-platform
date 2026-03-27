package com.example.chat.websocket.infrastructure.redis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 타이핑 이벤트 Redis Pub/Sub DTO
 *
 * 발행 채널: chat:typing:{channelId}
 * 발행자:   websocket-server (ChatWebSocketHandler)
 * 구독자:   websocket-server (TypingRedisSubscriber)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TypingEvent {

    /** 이벤트 타입: "TYPING_START" | "TYPING_STOP" */
    private String eventType;

    /** 채널 ID */
    private String channelId;

    /** 타이핑 중인 사용자 ID (String — UUID 지원) */
    private String senderId;
}
