package com.example.chat.message.usecase;

import com.example.chat.message.api.request.SendMessageRequest;
import com.example.chat.message.controller.MessagesResponse;

public interface MessageAppService {
    void sendMessage(String channelId, SendMessageRequest request);
    MessagesResponse fetchMessages(String channelId, String cursor, int limit);
}