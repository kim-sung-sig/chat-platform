package com.example.chat.websocket.infrastructure.redis;

import java.time.Instant;

import com.example.chat.domain.message.MessageStatus;
import com.example.chat.domain.message.MessageType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Redis에서 수신한 메시지 이벤트
 * chat-message-server의 MessageSentEvent와 동일한 구조
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageEvent {

	private String messageId; // String (UUID)
	private String channelId; // ChannelId
	private String senderId; // UserId
	private String messageType; // MessageType name (String)
	private String content; // 텍스트 내용
	private String status; // MessageStatus name (String)
	private Instant sentAt;

	/**
	 * MessageType enum 반환
	 */
	public MessageType getMessageTypeEnum() {
		if (messageType == null) {
			return null;
		}
		try {
			return MessageType.valueOf(messageType);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	/**
	 * MessageStatus enum 반환
	 */
	public MessageStatus getStatusEnum() {
		if (status == null) {
			return null;
		}
		try {
			return MessageStatus.valueOf(status);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
}
