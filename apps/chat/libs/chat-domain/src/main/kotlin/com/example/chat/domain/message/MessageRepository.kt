package com.example.chat.domain.message

import com.example.chat.domain.channel.ChannelId
import com.example.chat.domain.common.Cursor
import com.example.chat.domain.user.UserId

/**
 * 메시지 Repository 인터페이스 (포트)
 */
interface MessageRepository {

    /**
     * 메시지 저장
     */
    fun save(message: Message): Message

    /**
     * ID로 메시지 조회
     */
    fun findById(id: MessageId): Message?

    /**
     * 채널의 메시지 목록 조회 (커서 기반 페이징)
     */
    fun findByChannelId(channelId: ChannelId, cursor: Cursor, limit: Int): List<Message>

    /**
     * 특정 사용자가 보낸 메시지 목록 조회
     */
    fun findBySenderId(senderId: UserId, cursor: Cursor, limit: Int): List<Message>

    /**
     * 메시지 삭제
     */
    fun delete(id: MessageId)

    /**
     * 여러 채널의 마지막 메시지 배치 조회
     */
    fun findLastMessageByChannelIds(channelIds: List<ChannelId>): Map<ChannelId, Message>
}
