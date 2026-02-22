package com.example.chat.domain.message;

import java.time.Instant;

import com.example.chat.domain.channel.ChannelId;
import com.example.chat.domain.user.UserId;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 메시지 Aggregate Root
 *
 * 책임:
 * - 메시지 생명주기 관리
 * - 메시지 상태 전환 관리
 * - 비즈니스 규칙 검증
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public class Message {
    private MessageId id;
    private ChannelId channelId;
    private UserId senderId;
    private MessageContent content;
    private MessageType type;
    private MessageStatus status;
    private Instant createdAt;
    private Instant sentAt;
    private Instant deliveredAt;
    private Instant readAt;

    /**
     * 메시지를 발송 완료 상태로 변경
     */
    public void markAsSent() {
        if (status != MessageStatus.PENDING) {
            throw new IllegalStateException("Message must be in PENDING status to mark as SENT");
        }
        status = MessageStatus.SENT;
        sentAt = Instant.now();
    }

    /**
     * 메시지를 전달 완료 상태로 변경
     */
    public void markAsDelivered() {
        if (status != MessageStatus.SENT) {
            throw new IllegalStateException("Message must be in SENT status to mark as DELIVERED");
        }
        status = MessageStatus.DELIVERED;
        deliveredAt = Instant.now();
    }

    /**
     * 메시지를 읽음 상태로 변경
     */
    public void markAsRead() {
        if (status == MessageStatus.FAILED || status == MessageStatus.PENDING) {
            throw new IllegalStateException("Message must be delivered to mark as READ");
        }
        status = MessageStatus.READ;
        readAt = Instant.now();
    }

    /**
     * 메시지 발송 실패
     */
    public void markAsFailed() {
        status = MessageStatus.FAILED;
    }

    /**
     * 메시지를 편집할 수 있는지 확인
     */
    public boolean canBeEdited() {
        return status == MessageStatus.SENT ||
                status == MessageStatus.DELIVERED ||
                status == MessageStatus.READ;
    }

    /**
     * 메시지를 삭제할 수 있는지 확인
     */
    public boolean canBeDeleted() {
        return status != MessageStatus.FAILED;
    }

    /**
     * 새로운 메시지 생성
     */
    public static Message create(
            ChannelId channelId,
            UserId senderId,
            MessageContent content,
            MessageType type) {
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
     * Storage Layer에서 재구성
     */
    public static Message fromStorage(
            MessageId id,
            ChannelId channelId,
            UserId senderId,
            MessageContent content,
            MessageType type,
            MessageStatus status,
            Instant createdAt,
            Instant sentAt,
            Instant deliveredAt,
            Instant readAt) {
        return Message.builder()
                .id(id)
                .channelId(channelId)
                .senderId(senderId)
                .content(content)
                .type(type)
                .status(status)
                .createdAt(createdAt)
                .sentAt(sentAt)
                .deliveredAt(deliveredAt)
                .readAt(readAt)
                .build();
    }
}
