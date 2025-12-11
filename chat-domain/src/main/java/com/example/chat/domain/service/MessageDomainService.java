package com.example.chat.domain.service;

import com.example.chat.domain.channel.Channel;
import com.example.chat.domain.message.Message;
import com.example.chat.domain.message.MessageContent;
import com.example.chat.domain.message.MessageType;
import com.example.chat.domain.user.User;

/**
 * 메시지 도메인 서비스
 *
 * Domain Service의 역할:
 * 1. 여러 Aggregate 간의 협력을 조율
 * 2. 복잡한 도메인 규칙 검증
 * 3. 도메인 불변식(Invariants) 보장
 *
 * 이 서비스는 Channel, User, Message Aggregate 간의 협력을 통해
 * 메시지 발송 가능 여부를 검증하고 메시지를 생성합니다.
 */
public class MessageDomainService {

    /**
     * 텍스트 메시지 생성
     *
     * @param channel 메시지를 발송할 채널 (Aggregate)
     * @param sender 메시지를 발송하는 사용자 (Aggregate)
     * @param text 메시지 텍스트 내용
     * @return 생성된 메시지
     * @throws DomainException 도메인 규칙 위반 시
     */
    public Message createTextMessage(Channel channel, User sender, String text) {
        // Step 1: 도메인 규칙 검증 - 채널 접근 권한
        validateChannelAccess(channel, sender);

        // Step 2: 도메인 규칙 검증 - 메시지 발송 가능 여부
        validateMessageSendingCapability(channel, sender);

        // Step 3: 메시지 내용 검증
        validateTextContent(text);

        // Step 4: 메시지 생성
        MessageContent content = MessageContent.text(text);
        return Message.create(channel.getId(), sender.getId(), content, MessageType.TEXT);
    }

    /**
     * 이미지 메시지 생성
     */
    public Message createImageMessage(Channel channel, User sender, String mediaUrl, String fileName, Long fileSize) {
        // 도메인 규칙 검증
        validateChannelAccess(channel, sender);
        validateMessageSendingCapability(channel, sender);

        // 이미지 파일 크기 제한 (예: 10MB)
        validateImageFileSize(fileSize);
        validateMediaUrl(mediaUrl);

        MessageContent content = MessageContent.image(mediaUrl, fileName, fileSize);
        return Message.create(channel.getId(), sender.getId(), content, MessageType.IMAGE);
    }

    /**
     * 파일 메시지 생성
     */
    public Message createFileMessage(Channel channel, User sender, String mediaUrl, String fileName, Long fileSize, String mimeType) {
        // 도메인 규칙 검증
        validateChannelAccess(channel, sender);
        validateMessageSendingCapability(channel, sender);

        // 파일 크기 제한 (예: 50MB)
        validateFileSize(fileSize);
        validateMediaUrl(mediaUrl);
        validateFileName(fileName);

        MessageContent content = MessageContent.file(mediaUrl, fileName, fileSize, mimeType);
        return Message.create(channel.getId(), sender.getId(), content, MessageType.FILE);
    }

    /**
     * 시스템 메시지 생성 (관리자용)
     *
     * 시스템 메시지는 사용자 검증이 필요 없음
     */
    public Message createSystemMessage(Channel channel, String text) {
        // 채널 활성화 여부만 확인
        if (!channel.isActive()) {
            throw new DomainException("Cannot send system message to inactive channel");
        }

        validateTextContent(text);
        MessageContent content = MessageContent.text(text);

        // 시스템 계정으로 발송
        return Message.create(channel.getId(), User.SYSTEM_USER_ID, content, MessageType.SYSTEM);
    }

    // ========== 도메인 규칙 검증 메서드 ==========

    /**
     * 채널 접근 권한 검증
     *
     * 도메인 규칙:
     * - 사용자는 채널의 멤버여야 함
     * - 채널이 활성화되어 있어야 함
     */
    private void validateChannelAccess(Channel channel, User sender) {
        // Early return: 채널 활성화 확인
        if (!channel.isActive()) {
            throw new DomainException("Channel is not active");
        }

        // Early return: 멤버십 확인
        if (!channel.isMember(sender.getId())) {
            throw new DomainException("User is not a member of the channel");
        }
    }

    /**
     * 메시지 발송 가능 여부 검증
     *
     * 도메인 규칙:
     * - 사용자가 활성 상태여야 함
     * - 사용자가 차단되지 않았어야 함
     * - 사용자가 정지되지 않았어야 함
     */
    private void validateMessageSendingCapability(Channel channel, User sender) {
        // Early return: 사용자 상태 확인
        if (!sender.canSendMessage()) {
            throw new DomainException("User is not allowed to send messages (status: " + sender.getStatus() + ")");
        }

        if (sender.isBanned()) {
            throw new DomainException("User is banned and cannot send messages");
        }

        if (sender.isSuspended()) {
            throw new DomainException("User is suspended and cannot send messages");
        }
    }

    /**
     * 텍스트 내용 검증
     */
    private void validateTextContent(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Text content cannot be null or blank");
        }
        if (text.length() > 5000) {
            throw new IllegalArgumentException("Text content exceeds maximum length (5000 characters)");
        }
    }

    /**
     * 미디어 URL 검증
     */
    private void validateMediaUrl(String mediaUrl) {
        if (mediaUrl == null || mediaUrl.isBlank()) {
            throw new IllegalArgumentException("Media URL cannot be null or blank");
        }
        // URL 형식 검증 추가 가능
    }

    /**
     * 파일명 검증
     */
    private void validateFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("File name cannot be null or blank");
        }
        if (fileName.length() > 255) {
            throw new IllegalArgumentException("File name is too long (max 255 characters)");
        }
    }

    /**
     * 이미지 파일 크기 검증 (10MB 제한)
     */
    private void validateImageFileSize(Long fileSize) {
        if (fileSize == null || fileSize <= 0) {
            throw new IllegalArgumentException("File size must be positive");
        }

        long maxImageSize = 10 * 1024 * 1024; // 10MB
        if (fileSize > maxImageSize) {
            throw new DomainException("Image file size exceeds maximum allowed size (10MB)");
        }
    }

    /**
     * 파일 크기 검증 (50MB 제한)
     */
    private void validateFileSize(Long fileSize) {
        if (fileSize == null || fileSize <= 0) {
            throw new IllegalArgumentException("File size must be positive");
        }

        long maxFileSize = 50 * 1024 * 1024; // 50MB
        if (fileSize > maxFileSize) {
            throw new DomainException("File size exceeds maximum allowed size (50MB)");
        }
    }
}
