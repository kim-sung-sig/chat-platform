package com.example.chat.domain.message;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * 메시지 내용 (Value Object)
 */
@Getter
@ToString
@Builder
public class MessageContent {
    private final String text;
    private final String mediaUrl;
    private final String fileName;
    private final Long fileSize;
    private final String mimeType;

    public MessageContent(String text, String mediaUrl, String fileName, Long fileSize, String mimeType) {
        this.text = text;
        this.mediaUrl = mediaUrl;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
    }

    /**
     * 텍스트 메시지 생성
     */
    public static MessageContent text(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Text content cannot be null or blank");
        }
        return new MessageContent(text, null, null, null, null);
    }

    /**
     * 이미지 메시지 생성
     */
    public static MessageContent image(String mediaUrl, String fileName, Long fileSize) {
        if (mediaUrl == null || mediaUrl.isBlank()) {
            throw new IllegalArgumentException("Media URL cannot be null or blank");
        }
        return new MessageContent(null, mediaUrl, fileName, fileSize, "image/*");
    }

    /**
     * 파일 메시지 생성
     */
    public static MessageContent file(String mediaUrl, String fileName, Long fileSize, String mimeType) {
        if (mediaUrl == null || mediaUrl.isBlank()) {
            throw new IllegalArgumentException("Media URL cannot be null or blank");
        }
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("File name cannot be null or blank");
        }
        return new MessageContent(null, mediaUrl, fileName, fileSize, mimeType);
    }

    /**
     * 내용이 비어있는지 확인
     */
    public boolean isEmpty() {
        return (text == null || text.isBlank()) && (mediaUrl == null || mediaUrl.isBlank());
    }
}
