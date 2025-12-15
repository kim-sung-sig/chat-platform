package com.example.chat.domain.service;

import com.example.chat.domain.channel.Channel;
import com.example.chat.domain.channel.ChannelType;
import com.example.chat.domain.message.Message;
import com.example.chat.domain.message.MessageType;
import com.example.chat.domain.user.User;
import com.example.chat.domain.user.UserId;
import com.example.chat.domain.user.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * MessageDomainService 단위 테스트
 * <p>
 * 테스트 전략:
 * 1. 정상 케이스 (Happy Path)
 * 2. 도메인 규칙 위반 케이스
 * 3. 입력값 검증 실패 케이스
 * 4. 경계값 테스트
 */
@DisplayName("MessageDomainService 단위 테스트")
class MessageDomainServiceTest {

	private MessageDomainService messageDomainService;

	@BeforeEach
	void setUp() {
		messageDomainService = new MessageDomainService();
	}

	// ============================================================
	// 텍스트 메시지 생성 테스트
	// ============================================================

	private Channel createActiveChannel() {
		UserId testUserId = UserId.of("test-user-id");
		return Channel.create("Test Channel", ChannelType.GROUP, testUserId);
	}

	// ============================================================
	// 이미지 메시지 생성 테스트
	// ============================================================

	private Channel createInactiveChannel() {
		UserId testUserId = UserId.of("test-user-id");
		Channel channel = Channel.create("Inactive Channel", ChannelType.GROUP, testUserId);
		channel.deactivate();
		return channel;
	}

	// ============================================================
	// 파일 메시지 생성 테스트
	// ============================================================

	private User createActiveUser() {
		UserId testUserId = UserId.of("test-user-id");
		return User.builder()
				.id(testUserId)
				.username("Test User")
				.email("test@example.com")
				.status(UserStatus.ACTIVE)
				.createdAt(java.time.Instant.now())
				.updatedAt(java.time.Instant.now())
				.build();
	}

	// ============================================================
	// 시스템 메시지 생성 테스트
	// ============================================================

	private User createUserWithId(UserId userId) {
		return User.builder()
				.id(userId)
				.username("Test User")
				.email("test@example.com")
				.status(UserStatus.ACTIVE)
				.createdAt(java.time.Instant.now())
				.updatedAt(java.time.Instant.now())
				.build();
	}

	// ============================================================
	// 테스트 헬퍼 메서드
	// ============================================================

	private User createBannedUser() {
		UserId bannedUserId = UserId.of("test-user-id");
		User user = User.builder()
				.id(bannedUserId)
				.username("Banned User")
				.email("banned@example.com")
				.status(UserStatus.ACTIVE)
				.createdAt(java.time.Instant.now())
				.updatedAt(java.time.Instant.now())
				.build();
		user.ban();
		return user;
	}

	private User createSuspendedUser() {
		UserId suspendedUserId = UserId.of("test-user-id");
		User user = User.builder()
				.id(suspendedUserId)
				.username("Suspended User")
				.email("suspended@example.com")
				.status(UserStatus.ACTIVE)
				.createdAt(java.time.Instant.now())
				.updatedAt(java.time.Instant.now())
				.build();
		user.suspend();
		return user;
	}

	@Nested
	@DisplayName("텍스트 메시지 생성")
	class CreateTextMessage {

		@Test
		@DisplayName("정상: 활성 채널과 정상 사용자로 텍스트 메시지 생성")
		void success_createTextMessage() {
			// Given
			Channel channel = createActiveChannel();
			User sender = createActiveUser();
			String text = "Hello, World!";

			// When
			Message message = messageDomainService.createTextMessage(channel, sender, text);

			// Then
			assertThat(message).isNotNull();
			assertThat(message.getChannelId()).isEqualTo(channel.getId());
			assertThat(message.getSenderId()).isEqualTo(sender.getId());
			assertThat(message.getType()).isEqualTo(MessageType.TEXT);
			assertThat(message.getContent().getText()).isEqualTo(text);
		}

		@Test
		@DisplayName("실패: 텍스트 내용이 null")
		void fail_nullText() {
			// Given
			Channel channel = createActiveChannel();
			User sender = createActiveUser();

			// When & Then
			assertThatThrownBy(() -> messageDomainService.createTextMessage(channel, sender, null))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("Text content cannot be null or blank");
		}

		@Test
		@DisplayName("실패: 텍스트 내용이 빈 문자열")
		void fail_blankText() {
			// Given
			Channel channel = createActiveChannel();
			User sender = createActiveUser();

			// When & Then
			assertThatThrownBy(() -> messageDomainService.createTextMessage(channel, sender, "   "))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("Text content cannot be null or blank");
		}

		@Test
		@DisplayName("실패: 텍스트 길이 초과 (5000자 초과)")
		void fail_textTooLong() {
			// Given
			Channel channel = createActiveChannel();
			User sender = createActiveUser();
			String longText = "a".repeat(5001);

			// When & Then
			assertThatThrownBy(() -> messageDomainService.createTextMessage(channel, sender, longText))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("exceeds maximum length");
		}

		@Test
		@DisplayName("경계값: 텍스트 길이 5000자 (최대값)")
		void boundary_maxTextLength() {
			// Given
			Channel channel = createActiveChannel();
			User sender = createActiveUser();
			String maxText = "a".repeat(5000);

			// When
			Message message = messageDomainService.createTextMessage(channel, sender, maxText);

			// Then
			assertThat(message).isNotNull();
			assertThat(message.getContent().getText()).hasSize(5000);
		}

		@Test
		@DisplayName("실패: 채널이 비활성 상태")
		void fail_inactiveChannel() {
			// Given
			Channel inactiveChannel = createInactiveChannel();
			User sender = createActiveUser();

			// When & Then
			assertThatThrownBy(() -> messageDomainService.createTextMessage(inactiveChannel, sender, "Hello"))
					.isInstanceOf(DomainException.class)
					.hasMessageContaining("Channel is not active");
		}

		@Test
		@DisplayName("실패: 사용자가 채널 멤버가 아님")
		void fail_userNotMember() {
			// Given
			Channel channel = createActiveChannel();
			User nonMember = createUserWithId(UserId.of("non-member-user-id"));

			// When & Then
			assertThatThrownBy(() -> messageDomainService.createTextMessage(channel, nonMember, "Hello"))
					.isInstanceOf(DomainException.class)
					.hasMessageContaining("User is not a member of the channel");
		}

		@Test
		@DisplayName("실패: 사용자가 차단됨")
		void fail_bannedUser() {
			// Given
			Channel channel = createActiveChannel();
			User bannedUser = createBannedUser();

			// When & Then
			assertThatThrownBy(() -> messageDomainService.createTextMessage(channel, bannedUser, "Hello"))
					.isInstanceOf(DomainException.class)
					.hasMessageContaining("User is banned");
		}

		@Test
		@DisplayName("실패: 사용자가 정지됨")
		void fail_suspendedUser() {
			// Given
			Channel channel = createActiveChannel();
			User suspendedUser = createSuspendedUser();

			// When & Then
			assertThatThrownBy(() -> messageDomainService.createTextMessage(channel, suspendedUser, "Hello"))
					.isInstanceOf(DomainException.class)
					.hasMessageContaining("User is suspended");
		}
	}

	@Nested
	@DisplayName("이미지 메시지 생성")
	class CreateImageMessage {

		@Test
		@DisplayName("정상: 이미지 메시지 생성")
		void success_createImageMessage() {
			// Given
			Channel channel = createActiveChannel();
			User sender = createActiveUser();
			String imageUrl = "https://example.com/image.jpg";
			String fileName = "image.jpg";
			Long fileSize = 1024L * 1024L; // 1MB

			// When
			Message message = messageDomainService.createImageMessage(channel, sender, imageUrl, fileName, fileSize);

			// Then
			assertThat(message).isNotNull();
			assertThat(message.getType()).isEqualTo(MessageType.IMAGE);
			assertThat(message.getContent().getMediaUrl()).isEqualTo(imageUrl);
			assertThat(message.getContent().getFileName()).isEqualTo(fileName);
			assertThat(message.getContent().getFileSize()).isEqualTo(fileSize);
		}

		@Test
		@DisplayName("실패: 이미지 URL이 null")
		void fail_nullImageUrl() {
			// Given
			Channel channel = createActiveChannel();
			User sender = createActiveUser();

			// When & Then
			assertThatThrownBy(() -> messageDomainService.createImageMessage(channel, sender, null, "image.jpg", 1024L))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("Media URL cannot be null or blank");
		}

		@Test
		@DisplayName("실패: 파일 크기가 음수")
		void fail_negativFileSize() {
			// Given
			Channel channel = createActiveChannel();
			User sender = createActiveUser();

			// When & Then
			assertThatThrownBy(() -> messageDomainService.createImageMessage(
					channel, sender, "https://example.com/image.jpg", "image.jpg", -1L))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("File size must be positive");
		}

		@Test
		@DisplayName("실패: 이미지 파일 크기 초과 (10MB 초과)")
		void fail_imageSizeExceedsLimit() {
			// Given
			Channel channel = createActiveChannel();
			User sender = createActiveUser();
			Long oversizedFile = 11L * 1024L * 1024L; // 11MB

			// When & Then
			assertThatThrownBy(() -> messageDomainService.createImageMessage(
					channel, sender, "https://example.com/image.jpg", "image.jpg", oversizedFile))
					.isInstanceOf(DomainException.class)
					.hasMessageContaining("Image file size exceeds maximum allowed size");
		}

		@Test
		@DisplayName("경계값: 이미지 파일 크기 10MB (최대값)")
		void boundary_maxImageSize() {
			// Given
			Channel channel = createActiveChannel();
			User sender = createActiveUser();
			Long maxSize = 10L * 1024L * 1024L; // 10MB

			// When
			Message message = messageDomainService.createImageMessage(
					channel, sender, "https://example.com/image.jpg", "image.jpg", maxSize);

			// Then
			assertThat(message).isNotNull();
			assertThat(message.getContent().getFileSize()).isEqualTo(maxSize);
		}
	}

	@Nested
	@DisplayName("파일 메시지 생성")
	class CreateFileMessage {

		@Test
		@DisplayName("정상: 파일 메시지 생성")
		void success_createFileMessage() {
			// Given
			Channel channel = createActiveChannel();
			User sender = createActiveUser();
			String fileUrl = "https://example.com/document.pdf";
			String fileName = "document.pdf";
			Long fileSize = 5L * 1024L * 1024L; // 5MB
			String mimeType = "application/pdf";

			// When
			Message message = messageDomainService.createFileMessage(
					channel, sender, fileUrl, fileName, fileSize, mimeType);

			// Then
			assertThat(message).isNotNull();
			assertThat(message.getType()).isEqualTo(MessageType.FILE);
			assertThat(message.getContent().getMediaUrl()).isEqualTo(fileUrl);
			assertThat(message.getContent().getFileName()).isEqualTo(fileName);
			assertThat(message.getContent().getFileSize()).isEqualTo(fileSize);
		}

		@Test
		@DisplayName("실패: 파일명이 null")
		void fail_nullFileName() {
			// Given
			Channel channel = createActiveChannel();
			User sender = createActiveUser();

			// When & Then
			assertThatThrownBy(() -> messageDomainService.createFileMessage(
					channel, sender, "https://example.com/file", null, 1024L, "application/pdf"))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("File name cannot be null or blank");
		}

		@Test
		@DisplayName("실패: 파일명이 너무 김 (255자 초과)")
		void fail_fileNameTooLong() {
			// Given
			Channel channel = createActiveChannel();
			User sender = createActiveUser();
			String longFileName = "a".repeat(256) + ".pdf";

			// When & Then
			assertThatThrownBy(() -> messageDomainService.createFileMessage(
					channel, sender, "https://example.com/file", longFileName, 1024L, "application/pdf"))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("File name is too long");
		}

		@Test
		@DisplayName("실패: 파일 크기 초과 (50MB 초과)")
		void fail_fileSizeExceedsLimit() {
			// Given
			Channel channel = createActiveChannel();
			User sender = createActiveUser();
			Long oversizedFile = 51L * 1024L * 1024L; // 51MB

			// When & Then
			assertThatThrownBy(() -> messageDomainService.createFileMessage(
					channel, sender, "https://example.com/file", "file.zip", oversizedFile, "application/zip"))
					.isInstanceOf(DomainException.class)
					.hasMessageContaining("File size exceeds maximum allowed size");
		}

		@Test
		@DisplayName("경계값: 파일 크기 50MB (최대값)")
		void boundary_maxFileSize() {
			// Given
			Channel channel = createActiveChannel();
			User sender = createActiveUser();
			Long maxSize = 50L * 1024L * 1024L; // 50MB

			// When
			Message message = messageDomainService.createFileMessage(
					channel, sender, "https://example.com/file", "file.zip", maxSize, "application/zip");

			// Then
			assertThat(message).isNotNull();
			assertThat(message.getContent().getFileSize()).isEqualTo(maxSize);
		}
	}

	@Nested
	@DisplayName("시스템 메시지 생성")
	class CreateSystemMessage {

		@Test
		@DisplayName("정상: 시스템 메시지 생성")
		void success_createSystemMessage() {
			// Given
			Channel channel = createActiveChannel();
			String text = "System notification: New member joined";

			// When
			Message message = messageDomainService.createSystemMessage(channel, text);

			// Then
			assertThat(message).isNotNull();
			assertThat(message.getType()).isEqualTo(MessageType.SYSTEM);
			assertThat(message.getSenderId()).isEqualTo(User.SYSTEM_USER_ID);
			assertThat(message.getContent().getText()).isEqualTo(text);
		}

		@Test
		@DisplayName("실패: 비활성 채널에 시스템 메시지 발송")
		void fail_inactiveChannel() {
			// Given
			Channel inactiveChannel = createInactiveChannel();

			// When & Then
			assertThatThrownBy(() -> messageDomainService.createSystemMessage(inactiveChannel, "System message"))
					.isInstanceOf(DomainException.class)
					.hasMessageContaining("Cannot send system message to inactive channel");
		}

		@Test
		@DisplayName("실패: 시스템 메시지 텍스트가 null")
		void fail_nullText() {
			// Given
			Channel channel = createActiveChannel();

			// When & Then
			assertThatThrownBy(() -> messageDomainService.createSystemMessage(channel, null))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("Text content cannot be null or blank");
		}
	}
}
