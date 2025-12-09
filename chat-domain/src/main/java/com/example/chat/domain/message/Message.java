package com.example.chat.domain.message;

import com.example.chat.domain.channel.ChannelId;
import com.example.chat.domain.user.UserId;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * 메시지 Aggregate Root
 */
@Getter
@Builder
public class Message {
    private final MessageId id;
    private final ChannelId channelId;
    private final UserId senderId;
    private final MessageContent content;
    private final MessageType type;
    private MessageStatus status;
    private final Instant createdAt;
    private Instant sentAt;
    private Instant deliveredAt;
    private Instant readAt;

    /**
     * 새로운 메시지 생성
     */
    public static Message create(ChannelId channelId, UserId senderId, MessageContent content, MessageType type) {
        return Message.builder()
                .id(MessageId.generate())
                .channelId(channelId)
                .senderId(senderId)
                .content(content)
                .type(type)
                .status(MessageStatus.PENDING)
                .createdAt(Instant.now())
                .build();
    }

    /**
     * 메시지를 발송 완료 상태로 변경
     */
    public void markAsSent() {
        if (this.status != MessageStatus.PENDING) {
            throw new IllegalStateException("Message must be in PENDING status to mark as SENT");
        }
        this.status = MessageStatus.SENT;
        this.sentAt = Instant.now();
    }

    /**
     * 메시지를 전달 완료 상태로 변경
     */
    public void markAsDelivered() {
        if (this.status != MessageStatus.SENT) {
            throw new IllegalStateException("Message must be in SENT status to mark as DELIVERED");
        }
        this.status = MessageStatus.DELIVERED;
        this.deliveredAt = Instant.now();
    }

    /**
     * 메시지를 읽음 상태로 변경
     */
    public void markAsRead() {
        if (this.status == MessageStatus.FAILED || this.status == MessageStatus.PENDING) {
            throw new IllegalStateException("Message must be delivered to mark as READ");
        }
        this.status = MessageStatus.READ;
        this.readAt = Instant.now();
    }

    /**
     * 메시지 발송 실패
     */
    public void markAsFailed() {
        this.status = MessageStatus.FAILED;
    }

    /**
     * 메시지를 편집할 수 있는지 확인
     */
    public boolean canBeEdited() {
        return this.status == MessageStatus.SENT ||
               this.status == MessageStatus.DELIVERED ||
               this.status == MessageStatus.READ;
    }

    /**
     * 메시지를 삭제할 수 있는지 확인
     */
    public boolean canBeDeleted() {
        return this.status != MessageStatus.FAILED;
    }
}
