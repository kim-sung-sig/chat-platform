package com.example.chat.message.domain.impl;

import com.example.chat.common.dto.ChatMessage;
import com.example.chat.common.util.Cursor;
import com.example.chat.common.util.CursorCodec;
import com.example.chat.message.adapter.ChatMessageMapper;
import com.example.chat.message.domain.MessageDomainService;
import com.example.chat.storage.entity.ChatMessageEntity;
import com.example.chat.storage.entity.OutboxEventEntity;
import com.example.chat.storage.repository.ChatMessageRepository;
import com.example.chat.storage.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class MessageDomainServiceImpl implements MessageDomainService {

    private final ChatMessageRepository chatMessageRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public MessageDomainServiceImpl(ChatMessageRepository chatMessageRepository,
                                    OutboxEventRepository outboxEventRepository,
                                    ObjectMapper objectMapper) {
        this.chatMessageRepository = chatMessageRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public ChatMessage saveMessage(ChatMessage message) {
        ChatMessageEntity entity = ChatMessageMapper.toEntity(message);
        ChatMessageEntity saved = chatMessageRepository.save(entity);

        // Outbox: 트랜잭션 내에 이벤트를 함께 저장하여 일관성 보장
        try {
            Map<String, Object> payload = Map.of(
                    "messageId", saved.getId(),
                    "channelId", saved.getChannelId(),
                    "senderId", saved.getSenderId(),
                    "content", saved.getContent(),
                    "createdAt", saved.getCreatedAt() == null ? null : saved.getCreatedAt().toString()
            );
            String json = objectMapper.writeValueAsString(payload);

            OutboxEventEntity outbox = OutboxEventEntity.builder()
                    .aggregateId(String.valueOf(saved.getId()))
                    .eventType("MESSAGE_CREATED")
                    .payload(json)
                    .processed(false)
                    .build();

            outboxEventRepository.save(outbox);
        } catch (Exception e) {
            // Outbox 저장 실패는 심각하지만 메시지 저장을 롤백시키지 않도록 로그에 기록
            // TODO: 운영에서는 이 케이스에 대한 정책 결정 필요(재시도/롤백 등)
            throw new RuntimeException("Failed to write outbox event", e);
        }

        return ChatMessageMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChatMessage> fetchMessages(String channelId, String cursor, int limit) {
        Pageable pageable = PageRequest.of(0, Math.max(1, limit));
        if (cursor == null || cursor.isBlank()) {
            return chatMessageRepository.findByChannelIdOrderByCreatedAtDesc(channelId, pageable)
                    .map(ChatMessageMapper::toDto);
        }

        Cursor c = CursorCodec.decode(cursor);
        if (c == null || (c.getCreatedAt() == null && c.getId() == null)) {
            // invalid cursor -> treat as first page
            return chatMessageRepository.findByChannelIdOrderByCreatedAtDesc(channelId, pageable)
                    .map(ChatMessageMapper::toDto);
        }

        // cursor indicates last seen (createdAt,id) - fetch older messages
        return chatMessageRepository.findByChannelIdBefore(channelId, c.getCreatedAt(), c.getId(), pageable)
                .map(ChatMessageMapper::toDto);
    }
}