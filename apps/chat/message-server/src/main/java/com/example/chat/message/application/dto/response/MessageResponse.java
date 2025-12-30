package com.example.chat.message.application.dto.response;

import com.example.chat.domain.message.MessageStatus;
import com.example.chat.domain.message.MessageType;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * 메시지 응답 DTO
 */
@Getter
@Builder
public class MessageResponse {

    private String id;              // MessageId → String
    private String channelId;       // ChannelId
    private String senderId;        // UserId
    private MessageType messageType;
    private String content;         // 단순화 (text)
    private MessageStatus status;
    private Instant createdAt;
    private Instant sentAt;
    private Instant deliveredAt;
    private Instant readAt;
}
