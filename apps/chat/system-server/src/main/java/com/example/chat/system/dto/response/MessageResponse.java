package com.example.chat.system.dto.response;

import com.example.chat.domain.message.Message;
import com.example.chat.domain.message.MessageStatus;
import com.example.chat.domain.message.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * 메시지 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
public class MessageResponse {

	private String id;
	private String channelId;
	private String senderId;
	private MessageType messageType;
	private String content;
	private String mediaUrl;
	private String fileName;
	private Long fileSize;
	private String mimeType;
	private MessageStatus status;
	private Instant createdAt;
	private Instant sentAt;

	/**
	 * Domain Model → Response DTO 변환
	 */
	public static MessageResponse from(Message message) {
		return MessageResponse.builder()
				.id(message.getId().getValue())
				.channelId(message.getChannelId().getValue())
				.senderId(message.getSenderId().getValue())
				.messageType(message.getType())
				.content(message.getContent().getText())
				.mediaUrl(message.getContent().getMediaUrl())
				.fileName(message.getContent().getFileName())
				.fileSize(message.getContent().getFileSize())
				.mimeType(message.getContent().getMimeType())
				.status(message.getStatus())
				.createdAt(message.getCreatedAt())
				.sentAt(message.getSentAt())
				.build();
	}
}
