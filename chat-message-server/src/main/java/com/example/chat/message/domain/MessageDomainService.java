package com.example.chat.message.domain;

import com.example.chat.common.dto.ChatMessage;
import org.springframework.data.domain.Page;

public interface MessageDomainService {
    ChatMessage saveMessage(ChatMessage message);
    Page<ChatMessage> fetchMessages(String channelId, String cursor, int limit);
}