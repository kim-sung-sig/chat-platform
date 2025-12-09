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

    private String messageId;     // String (UUID)
    private String channelId;     // ChannelId
    private String senderId;      // UserId
    private String messageType;   // MessageType name
    private String content;       // 텍스트 내용
    private String status;        // MessageStatus name
    private Instant sentAt;
}
