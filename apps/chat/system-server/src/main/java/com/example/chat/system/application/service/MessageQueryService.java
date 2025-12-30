package com.example.chat.system.application.service;

import com.example.chat.common.auth.context.UserContextHolder;
import com.example.chat.domain.channel.ChannelId;
import com.example.chat.domain.common.Cursor;
import com.example.chat.domain.message.Message;
import com.example.chat.domain.message.MessageId;
import com.example.chat.domain.message.MessageRepository;
import com.example.chat.domain.user.UserId;
import com.example.chat.system.dto.response.CursorPageResponse;
import com.example.chat.system.dto.response.MessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 메시지 조회 애플리케이션 서비스 (Query Use Case)
 *
 * CQRS 패턴 적용:
 * - MessageApplicationService: Command (메시지 발송)
 * - MessageQueryService: Query (메시지 조회)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageQueryService {

	private final MessageRepository messageRepository;

	/**
	 * 채널의 메시지 목록 조회 (커서 기반 페이징)
	 *
	 * 흐름:
	 * 1. 인증 확인
	 * 2. Cursor 파싱
	 * 3. Repository 조회 (Cursor 기반)
	 * 4. 다음 Cursor 생성
	 * 5. DTO 변환
	 *
	 * @param channelIdStr 채널 ID
	 * @param cursorStr 커서 (null이면 첫 페이지)
	 * @param limit 조회할 메시지 수 (기본값: 20)
	 * @return 커서 페이징 응답
	 */
	public CursorPageResponse<MessageResponse> getMessages(String channelIdStr, String cursorStr, Integer limit) {
		log.info("Getting messages: channelId={}, cursor={}, limit={}", channelIdStr, cursorStr, limit);

		// Step 1: 입력값 검증
		if (channelIdStr == null || channelIdStr.isBlank()) {
			throw new IllegalArgumentException("Channel ID is required");
		}

		// Step 2: Limit 기본값 설정 및 검증
		int pageLimit = (limit != null && limit > 0) ? Math.min(limit, 100) : 20;

		// Step 3: Cursor 파싱
		Cursor cursor = parseCursor(cursorStr);

		// Step 4: Repository 조회
		ChannelId channelId = ChannelId.of(channelIdStr);
		List<Message> messages = messageRepository.findByChannelId(channelId, cursor, pageLimit + 1);

		// Step 5: hasNext 확인 (limit + 1개를 조회해서 확인)
		boolean hasNext = messages.size() > pageLimit;
		if (hasNext) {
			messages = messages.subList(0, pageLimit);
		}

		// Step 6: 다음 Cursor 생성
		String nextCursor = null;
		if (hasNext && !messages.isEmpty()) {
			Message lastMessage = messages.get(messages.size() - 1);
			nextCursor = createCursor(lastMessage);
		}

		// Step 7: DTO 변환
		List<MessageResponse> messageResponses = messages.stream()
				.map(this::convertToResponse)
				.collect(Collectors.toList());

		log.info("Messages retrieved: channelId={}, count={}, hasNext={}",
				channelIdStr, messageResponses.size(), hasNext);

		// Step 8: CursorPageResponse 생성
		return CursorPageResponse.<MessageResponse>builder()
				.data(messageResponses)
				.nextCursor(nextCursor)
				.hasNext(hasNext)
				.build();
	}

	/**
	 * 특정 메시지 조회
	 */
	public MessageResponse getMessage(String messageIdStr) {
		log.info("Getting message: messageId={}", messageIdStr);

		// Early Return: 입력값 검증
		if (messageIdStr == null || messageIdStr.isBlank()) {
			throw new IllegalArgumentException("Message ID is required");
		}

		// Repository 조회
		MessageId messageId = MessageId.of(messageIdStr);
		Message message = messageRepository.findById(messageId)
				.orElseThrow(() -> new IllegalArgumentException(
						"Message not found: " + messageIdStr
				));

		// DTO 변환
		return convertToResponse(message);
	}

	/**
	 * 읽지 않은 메시지 수 조회 (간단 구현)
	 *
	 * TODO: 실제로는 별도의 ReadReceipt 테이블이 필요함
	 */
	public long getUnreadMessageCount(String channelIdStr) {
		log.info("Getting unread message count: channelId={}", channelIdStr);

		// Early Return: 입력값 검증
		if (channelIdStr == null || channelIdStr.isBlank()) {
			throw new IllegalArgumentException("Channel ID is required");
		}

		// TODO: 실제 구현 필요
		// 현재는 단순히 전체 메시지 수를 반환
		ChannelId channelId = ChannelId.of(channelIdStr);
		List<Message> messages = messageRepository.findByChannelId(channelId, null, 1000);

		return messages.size();
	}

	// ============================================================
	// Private Helper Methods
	// ============================================================

	/**
	 * 인증된 사용자 ID 조회
	 */
	@SuppressWarnings("unused")
	private UserId getUserIdFromContext() {
		com.example.chat.common.auth.model.UserId authUserId = UserContextHolder.getUserId();

		// Early Return: 인증되지 않은 경우
		if (authUserId == null) {
			throw new IllegalStateException("User not authenticated");
		}

		return UserId.of(String.valueOf(authUserId.getValue()));
	}

	/**
	 * Cursor 문자열을 Cursor 객체로 파싱
	 *
	 * Cursor 형식: Base64(messageId:createdAtEpochMilli)
	 */
	private Cursor parseCursor(String cursorStr) {
		// Early Return: null이거나 빈 문자열이면 시작 커서
		if (cursorStr == null || cursorStr.isBlank()) {
			return Cursor.start();
		}

		try {
			// Base64 디코딩
			byte[] decoded = Base64.getDecoder().decode(cursorStr);
			String decodedStr = new String(decoded);

			// "messageId:timestamp" 형식으로 파싱
			String[] parts = decodedStr.split(":");
			if (parts.length != 2) {
				throw new IllegalArgumentException("Invalid cursor format");
			}

			// Instant로 변환하여 Cursor 생성
			String timestamp = parts[1];
			return Cursor.of(Instant.ofEpochMilli(Long.parseLong(timestamp)).toString());

		} catch (Exception e) {
			log.warn("Failed to parse cursor: {}", cursorStr, e);
			throw new IllegalArgumentException("Invalid cursor: " + cursorStr, e);
		}
	}

	/**
	 * Message로부터 Cursor 생성
	 *
	 * Cursor 형식: Base64(messageId:createdAtEpochMilli)
	 */
	private String createCursor(Message message) {
		try {
			String cursorData = message.getId().getValue() + ":" + message.getCreatedAt().toEpochMilli();
			return Base64.getEncoder().encodeToString(cursorData.getBytes());
		} catch (Exception e) {
			log.error("Failed to create cursor for message: {}", message.getId().getValue(), e);
			return null;
		}
	}

	/**
	 * Message Domain Model을 MessageResponse DTO로 변환
	 */
	private MessageResponse convertToResponse(Message message) {
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
