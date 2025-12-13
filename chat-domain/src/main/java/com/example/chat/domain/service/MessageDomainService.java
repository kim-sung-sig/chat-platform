package com.example.chat.domain.service;

import com.example.chat.domain.channel.Channel;
import com.example.chat.domain.message.Message;
import com.example.chat.domain.message.MessageContent;
import com.example.chat.domain.message.MessageType;
import com.example.chat.domain.user.User;

/**
 * 메시지 도메인 서비스
 *
 * DDD Domain Service의 역할:
 * 1. 여러 Aggregate Root 간의 협력을 조율
 * 2. 복잡한 도메인 규칙 검증 (단일 Aggregate으로 표현할 수 없는 규칙)
 * 3. 도메인 불변식(Invariants) 보장
 *
 * 이 서비스는 Channel + User Aggregate의 협력을 통해
 * 메시지 발송 가능 여부를 검증하고 Message를 생성합니다.
 */
public class MessageDomainService {

	/**
	 * 텍스트 메시지 생성
	 *
	 * Domain Service의 핵심:
	 * - Channel Aggregate: 채널 상태 및 멤버십 검증
	 * - User Aggregate: 사용자 상태 및 권한 검증
	 * - Message Aggregate: 메시지 생성
	 *
	 * @param channel 메시지를 발송할 채널 (Aggregate Root)
	 * @param sender 메시지를 발송하는 사용자 (Aggregate Root)
	 * @param text 메시지 텍스트 내용
	 * @return 생성된 메시지 (Aggregate Root)
	 * @throws DomainException 도메인 규칙 위반 시
	 */
	public Message createTextMessage(Channel channel, User sender, String text) {
		// Early Return: 텍스트 내용 사전 검증
		validateTextContent(text);

		// Domain Rule: Channel + User 협력을 통한 발송 권한 검증
		validateMessageSendingPermission(channel, sender);

		// Message 생성 (Aggregate 생성)
		MessageContent content = MessageContent.text(text);
		return Message.create(channel.getId(), sender.getId(), content, MessageType.TEXT);
	}

	/**
	 * 이미지 메시지 생성
	 */
	public Message createImageMessage(Channel channel, User sender, String mediaUrl, String fileName, Long fileSize) {
		// Early Return: 입력값 사전 검증
		validateMediaUrl(mediaUrl);
		validateImageFileSize(fileSize);

		// Domain Rule: 발송 권한 검증
		validateMessageSendingPermission(channel, sender);

		// Message 생성
		MessageContent content = MessageContent.image(mediaUrl, fileName, fileSize);
		return Message.create(channel.getId(), sender.getId(), content, MessageType.IMAGE);
	}

	/**
	 * 파일 메시지 생성
	 */
	public Message createFileMessage(Channel channel, User sender, String mediaUrl, String fileName, Long fileSize, String mimeType) {
		// Early Return: 입력값 사전 검증
		validateMediaUrl(mediaUrl);
		validateFileName(fileName);
		validateFileSize(fileSize);

		// Domain Rule: 발송 권한 검증
		validateMessageSendingPermission(channel, sender);

		// Message 생성
		MessageContent content = MessageContent.file(mediaUrl, fileName, fileSize, mimeType);
		return Message.create(channel.getId(), sender.getId(), content, MessageType.FILE);
	}

	/**
	 * 시스템 메시지 생성 (관리자/시스템용)
	 *
	 * 시스템 메시지는 User 검증이 불필요
	 */
	public Message createSystemMessage(Channel channel, String text) {
		// Early Return: 입력값 검증
		validateTextContent(text);

		// Early Return: 채널 상태 검증
		if (!channel.isActive()) {
			throw new DomainException("Cannot send system message to inactive channel");
		}

		// 시스템 계정으로 메시지 생성
		MessageContent content = MessageContent.text(text);
		return Message.create(channel.getId(), User.SYSTEM_USER_ID, content, MessageType.SYSTEM);
	}

	// ============================================================
	// 도메인 규칙 검증 메서드 (Domain Validation Logic)
	// ============================================================

	/**
	 * 메시지 발송 권한 검증 (핵심 도메인 규칙)
	 *
	 * 복합 도메인 규칙:
	 * 1. Channel: 활성 상태여야 함
	 * 2. Channel: 사용자가 멤버여야 함
	 * 3. User: 활성 상태여야 함 (차단/정지 아님)
	 */
	private void validateMessageSendingPermission(Channel channel, User sender) {
		// Early Return: 채널 활성화 확인
		if (!channel.isActive()) {
			throw new DomainException("Channel is not active");
		}

		// Early Return: 채널 멤버십 확인
		if (!channel.isMember(sender.getId())) {
			throw new DomainException("User is not a member of the channel");
		}

		// Early Return: 사용자 차단 여부 확인
		if (sender.isBanned()) {
			throw new DomainException("User is banned and cannot send messages");
		}

		// Early Return: 사용자 정지 여부 확인
		if (sender.isSuspended()) {
			throw new DomainException("User is suspended and cannot send messages");
		}

		// Early Return: 사용자 메시지 발송 가능 여부 확인
		if (!sender.canSendMessage()) {
			throw new DomainException("User is not allowed to send messages (status: " + sender.getStatus() + ")");
		}
	}

	// ============================================================
	// 입력값 검증 메서드 (Input Validation)
	// ============================================================

	/**
	 * 텍스트 내용 검증
	 */
	private void validateTextContent(String text) {
		// Early Return: null/blank 체크
		if (text == null || text.isBlank()) {
			throw new IllegalArgumentException("Text content cannot be null or blank");
		}

		// Early Return: 길이 제한 체크
		if (text.length() > 5000) {
			throw new IllegalArgumentException("Text content exceeds maximum length (5000 characters)");
		}
	}

	/**
	 * 미디어 URL 검증
	 */
	private void validateMediaUrl(String mediaUrl) {
		// Early Return
		if (mediaUrl == null || mediaUrl.isBlank()) {
			throw new IllegalArgumentException("Media URL cannot be null or blank");
		}
	}

	/**
	 * 파일명 검증
	 */
	private void validateFileName(String fileName) {
		// Early Return: null/blank 체크
		if (fileName == null || fileName.isBlank()) {
			throw new IllegalArgumentException("File name cannot be null or blank");
		}

		// Early Return: 길이 제한 체크
		if (fileName.length() > 255) {
			throw new IllegalArgumentException("File name is too long (max 255 characters)");
		}
	}

	/**
	 * 이미지 파일 크기 검증 (10MB 제한)
	 */
	private void validateImageFileSize(Long fileSize) {
		// Early Return: null/음수 체크
		if (fileSize == null || fileSize <= 0) {
			throw new IllegalArgumentException("File size must be positive");
		}

		// Early Return: 크기 제한 체크
		long maxImageSize = 10 * 1024 * 1024; // 10MB
		if (fileSize > maxImageSize) {
			throw new DomainException("Image file size exceeds maximum allowed size (10MB)");
		}
	}

	/**
	 * 파일 크기 검증 (50MB 제한)
	 */
	private void validateFileSize(Long fileSize) {
		// Early Return: null/음수 체크
		if (fileSize == null || fileSize <= 0) {
			throw new IllegalArgumentException("File size must be positive");
		}

		// Early Return: 크기 제한 체크
		long maxFileSize = 50 * 1024 * 1024; // 50MB
		if (fileSize > maxFileSize) {
			throw new DomainException("File size exceeds maximum allowed size (50MB)");
		}
	}
}
