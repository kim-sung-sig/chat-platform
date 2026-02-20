package com.example.chat.storage.adapter
import com.example.chat.domain.channel.ChannelId
import com.example.chat.domain.common.Cursor
import com.example.chat.domain.message.Message
import com.example.chat.domain.message.MessageId
import com.example.chat.domain.message.MessageRepository
import com.example.chat.domain.user.UserId
import com.example.chat.storage.mapper.MessageMapper
import com.example.chat.storage.repository.JpaMessageRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
@Repository
class MessageRepositoryAdapter(
    private val jpaRepository: JpaMessageRepository,
    private val mapper: MessageMapper
) : MessageRepository {
    @Transactional
    override fun save(message: Message): Message {
        val entity = mapper.toEntity(message)
        val saved = jpaRepository.save(entity)
        return mapper.toDomain(saved)
    }
    @Transactional(readOnly = true)
    override fun findById(id: MessageId): Message? {
        return jpaRepository.findById(id.value).orElse(null)?.let { mapper.toDomain(it) }
    }
    @Transactional(readOnly = true)
    override fun findByChannelId(channelId: ChannelId, cursor: Cursor, limit: Int): List<Message> {
        return jpaRepository.findByChannelIdOrderByCreatedAtDesc(channelId.value).take(limit).map { mapper.toDomain(it) }
    }
    @Transactional(readOnly = true)
    override fun findBySenderId(senderId: UserId, cursor: Cursor, limit: Int): List<Message> {
        return jpaRepository.findBySenderIdOrderByCreatedAtDesc(senderId.value).take(limit).map { mapper.toDomain(it) }
    }
    @Transactional
    override fun delete(id: MessageId) {
        jpaRepository.deleteById(id.value)
    }
    @Transactional(readOnly = true)
    override fun findLastMessageByChannelIds(channelIds: List<ChannelId>): Map<ChannelId, Message> {
        if (channelIds.isEmpty()) return emptyMap()
        return jpaRepository.findLastMessagesByChannelIds(channelIds.map { it.value })
            .associate { ChannelId.of(it.channelId) to mapper.toDomain(it) }
    }
}
