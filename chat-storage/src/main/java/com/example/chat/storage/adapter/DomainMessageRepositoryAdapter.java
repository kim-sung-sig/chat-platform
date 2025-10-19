package com.example.chat.storage.adapter;

import com.example.chat.domain.entity.ChatMessage;
import com.example.chat.domain.repository.MessageRepository;
import com.example.chat.storage.entity.ChatMessageEntity;
import com.example.chat.storage.repository.ChatMessageRepository;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
public class DomainMessageRepositoryAdapter implements MessageRepository {

    private final ChatMessageRepository delegate;

    public DomainMessageRepositoryAdapter(ChatMessageRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public ChatMessage save(ChatMessage message) {
        ChatMessageEntity entity = ChatMessageEntity.builder()
                .channelId(message.getChannelId())
                .senderId(message.getSenderId() == null ? null : message.getSenderId().get())
                .content(message.getContent())
                .messageStatus(message.getMessageStatus())
                .createdAt(message.getCreatedAt() == null ? OffsetDateTime.now() : message.getCreatedAt())
                .build();
        ChatMessageEntity saved = delegate.save(entity);
        return ChatMessage.builder()
                .id(saved.getId())
                .channelId(saved.getChannelId())
                .senderId(saved.getSenderId() == null ? null : com.example.chat.common.dto.UserId.of(saved.getSenderId()))
                .content(saved.getContent())
                .messageStatus(saved.getMessageStatus())
                .createdAt(saved.getCreatedAt())
                .build();
    }
}