package com.example.chat.storage.adapter;

import com.example.chat.domain.entity.ChatMessage;
import com.example.chat.domain.repository.MessageReader;
import com.example.chat.storage.entity.ChatMessageEntity;
import com.example.chat.storage.repository.ChatMessageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
public class DomainMessageReaderAdapter implements MessageReader {

    private final ChatMessageRepository delegate;

    public DomainMessageReaderAdapter(ChatMessageRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public Page<ChatMessage> fetchByChannelIdBefore(String channelId, Object cursor, int limit) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(0, Math.max(1, limit));
        if (cursor == null) {
            return delegate.findByChannelIdOrderByCreatedAtDesc(channelId, pageable).map(this::toDomain);
        }

        if (!(cursor instanceof java.util.Map)) {
            return delegate.findByChannelIdOrderByCreatedAtDesc(channelId, pageable).map(this::toDomain);
        }
        java.util.Map<?,?> map = (java.util.Map<?,?>) cursor;
        OffsetDateTime createdAt = (OffsetDateTime) map.get("createdAt");
        Long id = (Long) map.get("id");
        return delegate.findByChannelIdBefore(channelId, createdAt, id, pageable).map(this::toDomain);
    }

    private ChatMessage toDomain(ChatMessageEntity e) {
        return ChatMessage.builder()
                .id(e.getId())
                .channelId(e.getChannelId())
                .senderId(e.getSenderId() == null ? null : com.example.chat.common.dto.UserId.of(e.getSenderId()))
                .content(e.getContent())
                .messageStatus(e.getMessageStatus())
                .createdAt(e.getCreatedAt())
                .build();
    }
}