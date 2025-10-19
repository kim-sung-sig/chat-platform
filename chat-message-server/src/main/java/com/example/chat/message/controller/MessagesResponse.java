package com.example.chat.message.controller;

import com.example.chat.message.api.response.MessageResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class MessagesResponse {
    private final List<MessageResponse> messages;
    private final String nextCursor;
}