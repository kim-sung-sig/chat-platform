package com.example.chat.message.service;

import com.example.chat.common.dto.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

	private final MessagePublisher messagePublisher;

	public void sendMessage(Object message) {
		// 메시지 전송 로직 구현
		log.info("Sending message: {}", message);

		// 메시지 이벤트 발행
		final String topic = "chat.room." + "roomId"; // roomId는 실제 채팅방 ID로 대체
		messagePublisher.publish(topic, ChatMessage.builder().content("asfasf").build());
	}
}