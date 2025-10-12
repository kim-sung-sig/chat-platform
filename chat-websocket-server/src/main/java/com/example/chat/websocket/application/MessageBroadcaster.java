package com.example.chat.websocket.application;

import com.example.chat.common.dto.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageBroadcaster {

	private final SimpMessagingTemplate messageTemplate;

	public void broadcast(String roomId, ChatMessage chatMessage) {
		messageTemplate.convertAndSend(roomId, chatMessage);
	}

}