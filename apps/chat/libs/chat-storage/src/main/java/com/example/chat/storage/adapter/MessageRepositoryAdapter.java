package com.example.chat.storage.adapter;

import com.example.chat.domain.channel.ChannelId;
import com.example.chat.domain.common.Cursor;
import com.example.chat.domain.message.Message;
import com.example.chat.domain.message.MessageId;
import com.example.chat.domain.message.MessageRepository;
import com.example.chat.domain.user.UserId;
import com.example.chat.storage.mapper.MessageMapper;
import com.example.chat.storage.repository.JpaMessageRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class MessageRepositoryAdapter implements MessageRepository {
    private final JpaMessageRepository jpaRepository;
    private final MessageMapper mapper;

    public MessageRepositoryAdapter(JpaMessageRepository jpaRepository, MessageMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public Message save(Message message) {
        var entity = mapper.toEntity(message);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Message> findById(MessageId id) {
        return jpaRepository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Message> findByChannelId(ChannelId channelId, Cursor cursor, int limit) {
        // FIXME: Implement cursor-based pagination
        return jpaRepository.findByChannelIdOrderByCreatedAtDesc(channelId.value()).stream()
                .limit(limit)
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Message> findBySenderId(UserId senderId, Cursor cursor, int limit) {
        // FIXME: Implement cursor-based pagination
        return jpaRepository.findBySenderIdOrderByCreatedAtDesc(senderId.value()).stream()
                .limit(limit)
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(MessageId id) {
        jpaRepository.deleteById(id.value());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<ChannelId, Message> findLastMessageByChannelIds(List<ChannelId> channelIds) {
        if (channelIds.isEmpty())
            return Collections.emptyMap();
        List<String> ids = channelIds.stream().map(ChannelId::value).collect(Collectors.toList());
        return jpaRepository.findLastMessagesByChannelIds(ids).stream()
                .collect(Collectors.toMap(
                        it -> ChannelId.of(it.getChannelId()),
                        mapper::toDomain));
    }
}
