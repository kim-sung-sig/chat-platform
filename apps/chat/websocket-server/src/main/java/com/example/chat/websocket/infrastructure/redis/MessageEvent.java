package com.example.chat.websocket.infrastructure.redis;
import java.time.Instant;
import com.example.chat.common.core.enums.MessageStatus;
import com.example.chat.common.core.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
/**
 * Redis 에서 수신한 메시지 이벤트
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageEvent {
    private String messageId;
    private String channelId;
    private String senderId;
    private String messageType;
    private String content;
    private String status;
    private Instant sentAt;
    public MessageType getMessageTypeEnum() {
        if (messageType == null) return null;
        try { return MessageType.valueOf(messageType); } catch (IllegalArgumentException e) { return null; }
    }
    public MessageStatus getStatusEnum() {
        if (status == null) return null;
        try { return MessageStatus.valueOf(status); } catch (IllegalArgumentException e) { return null; }
    }
}