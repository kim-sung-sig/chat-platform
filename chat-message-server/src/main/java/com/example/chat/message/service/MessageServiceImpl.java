package com.example.chat.message.service;

import com.example.chat.common.dto.ChatMessage;
import com.example.chat.common.port.MessageService;
import com.example.chat.domain.repository.MessageReader;
import com.example.chat.domain.service.message.MessageService as DomainMessageService;
import com.example.chat.message.adapter.DomainToDtoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Production-ready flow의 시작점: 도메인 서비스에 위임하여 영속화/이벤트 발행을 처리합니다.
 * TODO: 현재는 동기 저장 후 바로 반환. Outbox 패턴 또는 비동기 발행으로 변경 권장.
 */
@Service
public class MessageServiceImpl implements MessageService {

    private static final Logger logger = LoggerFactory.getLogger(MessageServiceImpl.class);

    private final DomainMessageService domainMessageService;
    private final MessageReader messageReader;

    public MessageServiceImpl(DomainMessageService domainMessageService, MessageReader messageReader) {
        this.domainMessageService = domainMessageService;
        this.messageReader = messageReader;
    }

    @Override
    public void sendMessage(ChatMessage message) {
        logger.info("[MessageService] sendMessage channel={} sender={}", message.getRoomId(), message.getSenderId());
        // delegate to domain
        var domainMsg = new com.example.chat.domain.entity.ChatMessage(null, message.getRoomId(), message.getSenderId(), message.getContent(), "SENT", message.getSentAt());
        var saved = domainMessageService.sendMessage(domainMsg);
        logger.debug("message persisted id={} channel={} sender={}", saved.getId(), saved.getChannelId(), saved.getSenderId());
    }

    @Override
    public List<ChatMessage> fetchMessages(String channelId, String cursor, int limit) {
        // Use domain reader to fetch Page<domain.ChatMessage>
        var page = messageReader.fetchByChannelIdBefore(channelId, null, limit);
        return page.getContent().stream().map(DomainToDtoMapper::toDto).collect(Collectors.toList());
    }
}