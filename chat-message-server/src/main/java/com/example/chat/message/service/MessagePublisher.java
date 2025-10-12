package com.example.chat.message.service;

import com.example.chat.common.dto.ChatMessage;

public interface MessagePublisher {

	void publish(String topic, ChatMessage message);
}