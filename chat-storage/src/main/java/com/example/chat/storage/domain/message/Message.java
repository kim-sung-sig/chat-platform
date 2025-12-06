package com.example.chat.storage.domain.message;

import com.example.chat.common.auth.model.UserId;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

/**
 * Message Aggregate Root
 * DDD의 핵심 도메인 모델
 */
@Getter
@Builder(toBuilder = true)
public class Message {

    private final Long id;
    private final String roomId;
    private final String channelId;
    private final UserId senderId;
    private final MessageType messageType;
    private final MessageContent content;
    private final MessageStatus status;
    private final Instant sentAt;
    private final Instant updatedAt;
    private final Long replyToMessageId; // 답장 메시지 ID
    private final Boolean isEdited;
    private final Boolean isDeleted;

    /**
     * 메시지 전송 (도메인 이벤트)
     * 얼리 리턴 패턴 적용
     */
    public Message send() {
        // Early return: 검증 실패 시 즉시 반환
        if (this.status != MessageStatus.PENDING) {
            throw new IllegalStateException(
                String.format("Cannot send message in status: %s", this.status)
            );
        }

        return this.toBuilder()
                .status(MessageStatus.SENT)
                .sentAt(Instant.now())
                .build();
    }

    /**
     * 메시지 전달됨으로 상태 변경
     * 얼리 리턴 패턴 적용
     */
    public Message markAsDelivered() {
        // Early return: 잘못된 상태에서 호출 시 즉시 반환
        if (this.status != MessageStatus.SENT) {
            throw new IllegalStateException(
                String.format("Cannot mark as delivered in status: %s", this.status)
            );
        }

        return this.toBuilder()
                .status(MessageStatus.DELIVERED)
                .updatedAt(Instant.now())
                .build();
    }

    /**
     * 메시지 읽음으로 상태 변경
     * 얼리 리턴 패턴 적용
     */
    public Message markAsRead() {
        // Early return: 읽음 상태로 전환 불가능하면 즉시 반환
        if (!this.status.canTransitionToRead()) {
            throw new IllegalStateException(
                String.format("Cannot mark as read in status: %s", this.status)
            );
        }

        return this.toBuilder()
                .status(MessageStatus.READ)
                .updatedAt(Instant.now())
                .build();
    }

    /**
     * 메시지 실패로 상태 변경
     * 얼리 리턴 패턴 적용
     */
    public Message markAsFailed() {
        // Early return: 실패 상태로 전환 불가능하면 즉시 반환
        if (!this.status.canTransitionToFailed()) {
            throw new IllegalStateException(
                String.format("Cannot mark as failed in status: %s", this.status)
            );
        }

        return this.toBuilder()
                .status(MessageStatus.FAILED)
                .updatedAt(Instant.now())
                .build();
    }

    /**
     * 메시지 수정
     * 얼리 리턴 패턴 적용 - 모든 검증을 먼저 처리
     */
    public Message edit(MessageContent newContent) {
        // Early return 1: 삭제된 메시지는 수정 불가
        if (this.isDeleted) {
            throw new IllegalStateException("Cannot edit deleted message");
        }

        // Early return 2: 실패한 메시지는 수정 불가
        if (this.status == MessageStatus.FAILED) {
            throw new IllegalStateException("Cannot edit failed message");
        }

        // Early return 3: 메시지 타입 변경 불가
        if (newContent.getType() != this.messageType) {
            throw new IllegalArgumentException(
                String.format("Cannot change message type from %s to %s",
                    this.messageType, newContent.getType())
            );
        }

        // 모든 검증 통과 후 수정 진행
        return this.toBuilder()
                .content(newContent)
                .isEdited(true)
                .updatedAt(Instant.now())
                .build();
    }

    /**
     * 메시지 삭제 (소프트 삭제)
     * 얼리 리턴 패턴 적용
     */
    public Message delete() {
        // Early return: 이미 삭제된 메시지는 처리 불필요
        if (this.isDeleted) {
            throw new IllegalStateException("Message is already deleted");
        }

        return this.toBuilder()
                .isDeleted(true)
                .updatedAt(Instant.now())
                .build();
    }

    /**
     * 답장 메시지인지 확인
     */
    public boolean isReply() {
        return replyToMessageId != null;
    }

    /**
     * 도메인 불변식 검증
     */
    public void validate() {
        Objects.requireNonNull(roomId, "roomId must not be null");
        Objects.requireNonNull(senderId, "senderId must not be null");
        Objects.requireNonNull(messageType, "messageType must not be null");
        Objects.requireNonNull(content, "content must not be null");
        Objects.requireNonNull(status, "status must not be null");

        // 콘텐츠 검증
        content.validate();

        // 메시지 타입과 콘텐츠 타입 일치 확인
        if (content.getType() != messageType) {
            throw new IllegalStateException(
                String.format("Message type mismatch: %s != %s",
                    messageType, content.getType())
            );
        }
    }

    /**
     * 메시지 생성 팩토리 메서드
     */
    public static Message create(
            String roomId,
            String channelId,
            UserId senderId,
            MessageType messageType,
            MessageContent content
    ) {
        Message message = Message.builder()
                .roomId(roomId)
                .channelId(channelId)
                .senderId(senderId)
                .messageType(messageType)
                .content(content)
                .status(MessageStatus.PENDING)
                .sentAt(null)
                .updatedAt(Instant.now())
                .isEdited(false)
                .isDeleted(false)
                .build();

        message.validate();
        return message;
    }

    /**
     * 답장 메시지 생성
     */
    public static Message createReply(
            String roomId,
            String channelId,
            UserId senderId,
            MessageType messageType,
            MessageContent content,
            Long replyToMessageId
    ) {
        Objects.requireNonNull(replyToMessageId, "replyToMessageId must not be null");

        Message message = Message.builder()
                .roomId(roomId)
                .channelId(channelId)
                .senderId(senderId)
                .messageType(messageType)
                .content(content)
                .status(MessageStatus.PENDING)
                .replyToMessageId(replyToMessageId)
                .sentAt(null)
                .updatedAt(Instant.now())
                .isEdited(false)
                .isDeleted(false)
                .build();

        message.validate();
        return message;
    }
}
