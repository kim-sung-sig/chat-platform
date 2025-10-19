package com.example.chat.message.usecase.impl;

import com.example.chat.common.dto.ChatMessage;
import com.example.chat.common.port.MessageService;
import com.example.chat.common.util.Cursor;
import com.example.chat.common.util.CursorCodec;
import com.example.chat.message.api.request.SendMessageRequest;
import com.example.chat.message.api.response.MessageResponse;
import com.example.chat.message.controller.MessagesResponse;
import com.example.chat.message.usecase.MessageAppService;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MessageAppServiceImpl implements MessageAppService {

    private final MessageService messageService;

    public MessageAppServiceImpl(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public void sendMessage(String channelId, SendMessageRequest request) {
        ChatMessage dto = ChatMessage.builder()
                .roomId(channelId)
                .senderId(request.getSenderId())
                .content(request.getContent())
                .sentAt(request.getSentAt() == null ? OffsetDateTime.now() : request.getSentAt())
                .build();
        messageService.sendMessage(dto);
    }

    @Override
    public MessagesResponse fetchMessages(String channelId, String cursor, int limit) {
        // Use limit+1 to detect next page
        int fetchLimit = Math.max(1, limit);
        List<ChatMessage> list = messageService.fetchMessages(channelId, cursor, fetchLimit + 1);

        boolean hasNext = list.size() > fetchLimit;
        if (hasNext) {
            list = list.subList(0, fetchLimit);
        }

        List<MessageResponse> responses = list.stream()
                .map(m -> new MessageResponse(m.getId(), m.getRoomId(), m.getSenderId(), m.getContent(), m.getSentAt()))
                .collect(Collectors.toList());

        String nextCursor = null;
        if (!responses.isEmpty() && hasNext) {
            com.example.chat.message.api.response.MessageResponse last = responses.get(responses.size() - 1);
            Cursor next = new Cursor(last.getSentAt(), last.getId());
            nextCursor = CursorCodec.encode(next);
        }

        return new MessagesResponse(responses, nextCursor);
    }
}