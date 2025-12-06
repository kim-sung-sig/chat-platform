package com.example.chat.message.infrastructure.messaging;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * 메시지 발송 이벤트 DTO
 * Redis Pub/Sub으로 전송되는 이벤트
 */
@Getter
@Builder
public class MessageSentEvent {

    private Long messageId;
    private String roomId;
    private String channelId;
    private Long senderId;
    private String messageType;
    private String contentJson;
    private String status;
    private Instant sentAt;
    private Long replyToMessageId;
}
