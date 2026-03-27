package com.example.chat.storage.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 메시지 콘텐츠 값 객체.
 * chat_messages 테이블의 content_* 컬럼 그룹을 캡슐화한다.
 * 컬럼 이름은 ChatMessageEntity 의 기존 DDL 을 그대로 유지한다.
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MessageContent {

    @Column(name = "content_text", length = 5000)
    private String contentText;

    @Column(name = "content_media_url", length = 500)
    private String contentMediaUrl;

    @Column(name = "content_file_name", length = 255)
    private String contentFileName;

    @Column(name = "content_file_size")
    private Long contentFileSize;

    @Column(name = "content_mime_type", length = 100)
    private String contentMimeType;

    private MessageContent(String contentText, String contentMediaUrl,
                           String contentFileName, Long contentFileSize,
                           String contentMimeType) {
        this.contentText = contentText;
        this.contentMediaUrl = contentMediaUrl;
        this.contentFileName = contentFileName;
        this.contentFileSize = contentFileSize;
        this.contentMimeType = contentMimeType;
    }

    /**
     * 모든 콘텐츠 필드를 지정하는 팩토리 메서드.
     * 각 파라미터는 메시지 타입에 따라 일부가 null 일 수 있다.
     */
    public static MessageContent of(String text, String mediaUrl, String fileName,
                                    Long fileSize, String mimeType) {
        return new MessageContent(text, mediaUrl, fileName, fileSize, mimeType);
    }
}
