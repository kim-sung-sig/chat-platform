package com.example.chat.domain.service;

import com.example.chat.domain.channel.ChannelId;
import com.example.chat.domain.message.Message;
import com.example.chat.domain.message.MessageContent;
import com.example.chat.domain.message.MessageType;
import com.example.chat.domain.user.UserId;

/**
 * 메시지 도메인 서비스
 * 여러 Aggregate에 걸친 비즈니스 로직 처리
 */
public class MessageDomainService {

    /**
     * 텍스트 메시지 생성
     */
    public Message createTextMessage(ChannelId channelId, UserId senderId, String text) {
        validateTextContent(text);
        MessageContent content = MessageContent.text(text);
        return Message.create(channelId, senderId, content, MessageType.TEXT);
    }

    /**
     * 이미지 메시지 생성
     */
    public Message createImageMessage(ChannelId channelId, UserId senderId, String mediaUrl, String fileName, Long fileSize) {
        validateMediaUrl(mediaUrl);
        MessageContent content = MessageContent.image(mediaUrl, fileName, fileSize);
        return Message.create(channelId, senderId, content, MessageType.IMAGE);
    }

    /**
     * 파일 메시지 생성
     */
    public Message createFileMessage(ChannelId channelId, UserId senderId, String mediaUrl, String fileName, Long fileSize, String mimeType) {
        validateMediaUrl(mediaUrl);
        validateFileName(fileName);
        MessageContent content = MessageContent.file(mediaUrl, fileName, fileSize, mimeType);
        return Message.create(channelId, senderId, content, MessageType.FILE);
    }

    /**
     * 시스템 메시지 생성
     */
    public Message createSystemMessage(ChannelId channelId, String text) {
        validateTextContent(text);
        MessageContent content = MessageContent.text(text);
        // 시스템 메시지는 senderId가 없음 (null 또는 시스템 계정)
        return Message.create(channelId, UserId.of("system"), content, MessageType.SYSTEM);
    }

    /**
     * 텍스트 내용 검증
     */
    private void validateTextContent(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Text content cannot be null or blank");
        }
        if (text.length() > 5000) {
            throw new IllegalArgumentException("Text content exceeds maximum length (5000)");
        }
    }

    /**
     * 미디어 URL 검증
     */
    private void validateMediaUrl(String mediaUrl) {
        if (mediaUrl == null || mediaUrl.isBlank()) {
            throw new IllegalArgumentException("Media URL cannot be null or blank");
        }
    }

    /**
     * 파일명 검증
     */
    private void validateFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("File name cannot be null or blank");
        }
    }
}
