package com.example.chat.message.application.service;

import com.example.chat.common.auth.context.UserContextHolder;
import com.example.chat.domain.channel.Channel;
import com.example.chat.domain.channel.ChannelId;
import com.example.chat.domain.channel.ChannelRepository;
import com.example.chat.domain.message.Message;
import com.example.chat.domain.message.MessageRepository;
import com.example.chat.domain.message.MessageType;
import com.example.chat.domain.service.MessageDomainService;
import com.example.chat.domain.user.User;
import com.example.chat.domain.user.UserId;
import com.example.chat.domain.user.UserRepository;
import com.example.chat.message.application.dto.request.SendMessageRequest;
import com.example.chat.message.application.dto.response.MessageResponse;
import com.example.chat.message.infrastructure.messaging.MessageEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 메시지 애플리케이션 서비스 (Use Case)
 *
 * Application Service의 역할:
 * 1. 트랜잭션 경계 관리
 * 2. 인증/인가 확인
 * 3. Repository에서 Aggregate 조회
 * 4. Domain Service 호출 (도메인 로직은 Domain Service에 위임)
 * 5. 이벤트 발행
 * 6. DTO 변환
 *
 * 비즈니스 규칙 검증은 Domain Service에서 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageApplicationService {

	private final MessageRepository messageRepository;
	private final ChannelRepository channelRepository;
	private final UserRepository userRepository;
	private final MessageDomainService messageDomainService;
	private final MessageEventPublisher messageEventPublisher;

	/**
	 * 메시지 발송 Use Case
	 *
	 * 흐름:
	 * 1. 인증 확인
	 * 2. Aggregate 조회 (Channel, User)
	 * 3. Domain Service 호출 (비즈니스 규칙 검증 + 메시지 생성)
	 * 4. 저장
	 * 5. 이벤트 발행
	 */
	@Transactional
	public MessageResponse sendMessage(SendMessageRequest request) {
		log.info("Sending message: channelId={}, type={}", request.getChannelId(), request.getMessageType());

		// Step 1: 인증 확인 - 인증된 사용자 ID 조회
		UserId senderId = getUserIdFromContext();

		// Step 2: 필수 파라미터 검증
		if (request.getChannelId() == null || request.getChannelId().isBlank()) {
			throw new IllegalArgumentException("Channel ID is required");
		}

		// Step 3: Aggregate 조회 - Channel
		Channel channel = findChannelById(request.getChannelId());

		// Step 4: Aggregate 조회 - User
		User sender = findUserById(senderId);

		// Step 5: Domain Service 호출 - 메시지 생성 (도메인 규칙 검증 포함)
		Message message = createMessageByType(channel, sender, request);

		// Step 6: 저장
		Message savedMessage = messageRepository.save(message);

		// Step 7: 이벤트 발행 (비동기)
		publishMessageEvent(savedMessage);

		// Step 8: Response 변환
		MessageResponse response = convertToResponse(savedMessage);

		log.info("Message sent successfully: messageId={}, channelId={}, senderId={}",
				savedMessage.getId().getValue(), channel.getId().getValue(), sender.getId().getValue());

		return response;
	}

	// ========== Private Helper Methods ==========

	/**
	 * 인증된 사용자 ID 조회
	 */
	private UserId getUserIdFromContext() {
		com.example.chat.common.auth.model.UserId authUserId = UserContextHolder.getUserId();
		if (authUserId == null) {
			throw new IllegalStateException("User not authenticated");
		}
		return UserId.of(String.valueOf(authUserId.getValue()));
	}

	/**
	 * Channel Aggregate 조회
	 */
	private Channel findChannelById(String channelIdStr) {
		ChannelId channelId = ChannelId.of(channelIdStr);
		return channelRepository.findById(channelId)
				.orElseThrow(() -> new IllegalArgumentException("Channel not found: " + channelIdStr));
	}

	/**
	 * User Aggregate 조회
	 */
	private User findUserById(UserId userId) {
		return userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("User not found: " + userId.getValue()));
	}

	/**
	 * MessageType에 따라 메시지 생성
	 *
	 * Domain Service에 Aggregate를 전달하여 도메인 규칙 검증 수행
	 */
	private Message createMessageByType(Channel channel, User sender, SendMessageRequest request) {
		MessageType type = request.getMessageType();

		// Early return: MessageType 검증
		if (type == null) {
			throw new IllegalArgumentException("Message type is required");
		}

		switch (type) {
			case TEXT:
				String text = extractTextField(request, "text");
				return messageDomainService.createTextMessage(channel, sender, text);

			case IMAGE:
				String imageUrl = extractTextField(request, "imageUrl");
				String imageName = extractTextFieldOrDefault(request, "fileName", "image.jpg");
				Long imageSize = extractLongFieldOrDefault(request, "fileSize", 0L);
				return messageDomainService.createImageMessage(channel, sender, imageUrl, imageName, imageSize);

			case FILE:
				String fileUrl = extractTextField(request, "fileUrl");
				String fileName = extractTextField(request, "fileName");
				Long fileSize = extractLongFieldOrDefault(request, "fileSize", 0L);
				String mimeType = extractTextFieldOrDefault(request, "mimeType", "application/octet-stream");
				return messageDomainService.createFileMessage(channel, sender, fileUrl, fileName, fileSize, mimeType);

			case SYSTEM:
				// 시스템 메시지는 User가 필요 없음
				String systemText = extractTextField(request, "text");
				return messageDomainService.createSystemMessage(channel, systemText);

			default:
				throw new IllegalArgumentException("Unsupported message type: " + type);
		}
	}

	/**
	 * Payload에서 필수 텍스트 필드 추출
	 */
	private String extractTextField(SendMessageRequest request, String fieldName) {
		if (request.getPayload() == null) {
			throw new IllegalArgumentException("Payload is required");
		}

		Object value = request.getPayload().get(fieldName);
		if (value == null) {
			throw new IllegalArgumentException(String.format("Field '%s' is required in payload", fieldName));
		}

		return value.toString();
	}

	/**
	 * Payload에서 텍스트 필드 추출 (기본값 있음)
	 */
	private String extractTextFieldOrDefault(SendMessageRequest request, String fieldName, String defaultValue) {
		if (request.getPayload() == null) {
			return defaultValue;
		}

		Object value = request.getPayload().get(fieldName);
		return value != null ? value.toString() : defaultValue;
	}

	/**
	 * Payload에서 Long 필드 추출 (기본값 있음)
	 */
	private Long extractLongFieldOrDefault(SendMessageRequest request, String fieldName, Long defaultValue) {
		if (request.getPayload() == null) {
			return defaultValue;
		}

		Object value = request.getPayload().get(fieldName);
		if (value == null) {
			return defaultValue;
		}

		if (value instanceof Number) {
			return ((Number) value).longValue();
		}

		try {
			return Long.parseLong(value.toString());
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	/**
	 * 이벤트 발행
	 */
	private void publishMessageEvent(Message message) {
		try {
			messageEventPublisher.publishMessageSent(message);
		} catch (Exception e) {
			log.error("Failed to publish message event: messageId={}", message.getId().getValue(), e);
			// 이벤트 발행 실패는 메시지 발송을 막지 않음
		}
	}

	/**
	 * Message 도메인을 Response DTO로 변환
	 */
	private MessageResponse convertToResponse(Message message) {
		return MessageResponse.builder()
				.id(message.getId().getValue())
				.channelId(message.getChannelId().getValue())
				.senderId(message.getSenderId().getValue())
				.messageType(message.getType())
				.content(message.getContent().getText())  // 간단히 text만 반환
				.status(message.getStatus())
				.createdAt(message.getCreatedAt())
				.sentAt(message.getSentAt())
				.build();
	}
}
