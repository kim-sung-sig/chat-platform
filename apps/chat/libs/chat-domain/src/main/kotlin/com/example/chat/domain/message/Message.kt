package com.example.chat.domain.message

import com.example.chat.domain.channel.ChannelId
import com.example.chat.domain.user.UserId
import java.time.Instant

/**
 * 메시지 Aggregate Root
 *
 * 책임:
 * - 메시지 생명주기 관리
 * - 메시지 상태 전환 관리
 * - 비즈니스 규칙 검증
 */
class Message private constructor(
    val id: MessageId,
    val channelId: ChannelId,
    val senderId: UserId,
    val content: MessageContent,
    val type: MessageType,
    private var _status: MessageStatus,
    val createdAt: Instant,
    private var _sentAt: Instant? = null,
    private var _deliveredAt: Instant? = null,
    private var _readAt: Instant? = null
) {
    val status: MessageStatus get() = _status
    val sentAt: Instant? get() = _sentAt
    val deliveredAt: Instant? get() = _deliveredAt
    val readAt: Instant? get() = _readAt

    /**
     * 메시지를 발송 완료 상태로 변경
     */
    fun markAsSent() {
        check(_status == MessageStatus.PENDING) {
            "Message must be in PENDING status to mark as SENT"
        }
        _status = MessageStatus.SENT
        _sentAt = Instant.now()
    }

    /**
     * 메시지를 전달 완료 상태로 변경
     */
    fun markAsDelivered() {
        check(_status == MessageStatus.SENT) {
            "Message must be in SENT status to mark as DELIVERED"
        }
        _status = MessageStatus.DELIVERED
        _deliveredAt = Instant.now()
    }

    /**
     * 메시지를 읽음 상태로 변경
     */
    fun markAsRead() {
        check(_status != MessageStatus.FAILED && _status != MessageStatus.PENDING) {
            "Message must be delivered to mark as READ"
        }
        _status = MessageStatus.READ
        _readAt = Instant.now()
    }

    /**
     * 메시지 발송 실패
     */
    fun markAsFailed() {
        _status = MessageStatus.FAILED
    }

    /**
     * 메시지를 편집할 수 있는지 확인
     */
    fun canBeEdited(): Boolean =
        _status == MessageStatus.SENT ||
        _status == MessageStatus.DELIVERED ||
        _status == MessageStatus.READ

    /**
     * 메시지를 삭제할 수 있는지 확인
     */
    fun canBeDeleted(): Boolean = _status != MessageStatus.FAILED

    companion object {
        /**
         * 새로운 메시지 생성
         */
        fun create(
            channelId: ChannelId,
            senderId: UserId,
            content: MessageContent,
            type: MessageType
        ): Message {
            return Message(
                id = MessageId.generate(),
                channelId = channelId,
                senderId = senderId,
                content = content,
                type = type,
                _status = MessageStatus.PENDING,
                createdAt = Instant.now()
            )
        }

        /**
         * Storage Layer에서 재구성
         * Entity로부터 도메인 모델 복원 시 사용
         */
        @JvmStatic
        fun fromStorage(
            id: MessageId,
            channelId: ChannelId,
            senderId: UserId,
            content: MessageContent,
            type: MessageType,
            status: MessageStatus,
            createdAt: Instant,
            sentAt: Instant?,
            deliveredAt: Instant?,
            readAt: Instant?
        ): Message {
            return Message(
                id = id,
                channelId = channelId,
                senderId = senderId,
                content = content,
                type = type,
                _status = status,
                createdAt = createdAt,
                _sentAt = sentAt,
                _deliveredAt = deliveredAt,
                _readAt = readAt
            )
        }
    }
}

