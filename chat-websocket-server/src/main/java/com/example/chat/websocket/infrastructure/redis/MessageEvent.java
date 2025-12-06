package com.example.chat.websocket.infrastructure.redis;

import com.example.chat.storage.domain.message.MessageStatus;
import com.example.chat.storage.domain.message.MessageType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Redis에서 수신한 메시지 이벤트
 * chat-message-server의 MessageSentEvent와 동일한 구조
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageEvent {

    private Long messageId;
    private String roomId;
    private String channelId;
    private Long senderId;

    @JsonProperty("messageType")
    private String messageTypeCode;  // JSON에서는 String으로 받음

    private String contentJson;

    @JsonProperty("status")
    private String statusCode;  // JSON에서는 String으로 받음

    private Instant sentAt;
    private Long replyToMessageId;

    /**
     * MessageType enum 반환
     */
    public MessageType getMessageType() {
        if (messageTypeCode == null) {
            return null;
        }
        return MessageType.fromCode(messageTypeCode);
    }

    /**
     * MessageStatus enum 반환
     */
    public MessageStatus getStatus() {
        if (statusCode == null) {
            return null;
        }
        return MessageStatus.fromCode(statusCode);
    }
}
