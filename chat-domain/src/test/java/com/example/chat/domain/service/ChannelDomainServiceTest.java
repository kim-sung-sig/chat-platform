package com.example.chat.domain.service;

import com.example.chat.domain.channel.Channel;
import com.example.chat.domain.channel.ChannelType;
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
 * ChannelDomainService 단위 테스트
 */
@DisplayName("ChannelDomainService 단위 테스트")
class ChannelDomainServiceTest {

	private ChannelDomainService channelDomainService;

	@BeforeEach
	void setUp() {
		channelDomainService = new ChannelDomainService();
	}

	// ============================================================
	// 일대일 채널 생성 테스트
	// ============================================================

	private User createUser(UserId userId) {
		return User.builder()
				.id(userId)
				.username("Test User " + userId.getValue())
				.email(userId.getValue() + "@example.com")
				.status(UserStatus.ACTIVE)
				.createdAt(java.time.Instant.now())
				.updatedAt(java.time.Instant.now())
				.build();
	}

	// ============================================================
	// 그룹 채널 생성 테스트
	// ============================================================

	@Nested
	@DisplayName("일대일 채널 생성")
	class CreateDirectChannel {

		@Test
		@DisplayName("정상: 두 사용자로 일대일 채널 생성")
		void success_createDirectChannel() {
			// Given
			User user1 = createUser(UserId.of("user1"));
			User user2 = createUser(UserId.of("user2"));

			// When
			Channel channel = channelDomainService.createDirectChannel(user1, user2);

			// Then
			assertThat(channel).isNotNull();
			assertThat(channel.getType()).isEqualTo(ChannelType.DIRECT);
			assertThat(channel.getOwnerId()).isEqualTo(user1.getId());
			assertThat(channel.isMember(user1.getId())).isTrue();
			assertThat(channel.isMember(user2.getId())).isTrue();
		}

		@Test
		@DisplayName("실패: 동일한 사용자로 일대일 채널 생성 시도")
		void fail_sameUser() {
			// Given
			User user = createUser(UserId.of("user1"));

			// When & Then
			assertThatThrownBy(() -> channelDomainService.createDirectChannel(user, user))
					.isInstanceOf(DomainException.class)
					.hasMessageContaining("Cannot create direct channel with same user");
		}

		@Test
		@DisplayName("실패: user1이 활성 상태가 아님")
		void fail_user1NotActive() {
			// Given
			User user1 = createUser(UserId.of("user1"));
			user1.suspend();
			User user2 = createUser(UserId.of("user2"));

			// When & Then
			assertThatThrownBy(() -> channelDomainService.createDirectChannel(user1, user2))
					.isInstanceOf(DomainException.class)
					.hasMessageContaining("User1 is not in active status");
		}

		@Test
		@DisplayName("실패: user2가 활성 상태가 아님")
		void fail_user2NotActive() {
			// Given
			User user1 = createUser(UserId.of("user1"));
			User user2 = createUser(UserId.of("user2"));
			user2.ban();

			// When & Then
			assertThatThrownBy(() -> channelDomainService.createDirectChannel(user1, user2))
					.isInstanceOf(DomainException.class)
					.hasMessageContaining("User2 is not in active status");
		}
	}

	// ============================================================
	// 공개 채널 생성 테스트
	// ============================================================

	@Nested
	@DisplayName("그룹 채널 생성")
	class CreateGroupChannel {

		@Test
		@DisplayName("정상: 그룹 채널 생성")
		void success_createGroupChannel() {
			// Given
			User owner = createUser(UserId.of("owner"));
			String channelName = "My Group Chat";

			// When
			Channel channel = channelDomainService.createGroupChannel(channelName, owner);

			// Then
			assertThat(channel).isNotNull();
			assertThat(channel.getType()).isEqualTo(ChannelType.GROUP);
			assertThat(channel.getName()).isEqualTo(channelName);
			assertThat(channel.getOwnerId()).isEqualTo(owner.getId());
			assertThat(channel.isMember(owner.getId())).isTrue();
		}

		@Test
		@DisplayName("실패: 채널명이 null")
		void fail_nullChannelName() {
			// Given
			User owner = createUser(UserId.of("owner"));

			// When & Then
			assertThatThrownBy(() -> channelDomainService.createGroupChannel(null, owner))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("Channel name cannot be null or blank");
		}

		@Test
		@DisplayName("실패: 채널명이 빈 문자열")
		void fail_blankChannelName() {
			// Given
			User owner = createUser(UserId.of("owner"));

			// When & Then
			assertThatThrownBy(() -> channelDomainService.createGroupChannel("   ", owner))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("Channel name cannot be null or blank");
		}

		@Test
		@DisplayName("실패: 채널명이 너무 김 (100자 초과)")
		void fail_channelNameTooLong() {
			// Given
			User owner = createUser(UserId.of("owner"));
			String longName = "a".repeat(101);

			// When & Then
			assertThatThrownBy(() -> channelDomainService.createGroupChannel(longName, owner))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("exceeds maximum length");
		}

		@Test
		@DisplayName("경계값: 채널명 100자 (최대값)")
		void boundary_maxChannelNameLength() {
			// Given
			User owner = createUser(UserId.of("owner"));
			String maxLengthName = "a".repeat(100);

			// When
			Channel channel = channelDomainService.createGroupChannel(maxLengthName, owner);

			// Then
			assertThat(channel).isNotNull();
			assertThat(channel.getName()).hasSize(100);
		}

		@Test
		@DisplayName("실패: 소유자가 활성 상태가 아님")
		void fail_ownerNotActive() {
			// Given
			User owner = createUser(UserId.of("owner"));
			owner.suspend();

			// When & Then
			assertThatThrownBy(() -> channelDomainService.createGroupChannel("Test Channel", owner))
					.isInstanceOf(DomainException.class)
					.hasMessageContaining("Owner is not in active status");
		}
	}

	// ============================================================
	// 비공개 채널 생성 테스트
	// ============================================================

	@Nested
	@DisplayName("공개 채널 생성")
	class CreatePublicChannel {

		@Test
		@DisplayName("정상: 공개 채널 생성")
		void success_createPublicChannel() {
			// Given
			User owner = createUser(UserId.of("owner"));
			String channelName = "Public Announcements";

			// When
			Channel channel = channelDomainService.createPublicChannel(channelName, owner);

			// Then
			assertThat(channel).isNotNull();
			assertThat(channel.getType()).isEqualTo(ChannelType.PUBLIC);
			assertThat(channel.getName()).isEqualTo(channelName);
			assertThat(channel.getOwnerId()).isEqualTo(owner.getId());
		}

		@Test
		@DisplayName("실패: 소유자가 활성 상태가 아님")
		void fail_ownerNotActive() {
			// Given
			User owner = createUser(UserId.of("owner"));
			owner.ban();

			// When & Then
			assertThatThrownBy(() -> channelDomainService.createPublicChannel("Public Channel", owner))
					.isInstanceOf(DomainException.class)
					.hasMessageContaining("Owner is not in active status");
		}
	}

	// ============================================================
	// 멤버 추가 테스트
	// ============================================================

	@Nested
	@DisplayName("비공개 채널 생성")
	class CreatePrivateChannel {

		@Test
		@DisplayName("정상: 비공개 채널 생성")
		void success_createPrivateChannel() {
			// Given
			User owner = createUser(UserId.of("owner"));
			String channelName = "Secret Project";

			// When
			Channel channel = channelDomainService.createPrivateChannel(channelName, owner);

			// Then
			assertThat(channel).isNotNull();
			assertThat(channel.getType()).isEqualTo(ChannelType.PRIVATE);
			assertThat(channel.getName()).isEqualTo(channelName);
			assertThat(channel.getOwnerId()).isEqualTo(owner.getId());
		}

		@Test
		@DisplayName("실패: 소유자가 활성 상태가 아님")
		void fail_ownerNotActive() {
			// Given
			User owner = createUser(UserId.of("owner"));
			owner.suspend();

			// When & Then
			assertThatThrownBy(() -> channelDomainService.createPrivateChannel("Private Channel", owner))
					.isInstanceOf(DomainException.class)
					.hasMessageContaining("Owner is not in active status");
		}
	}

	// ============================================================
	// 멤버 제거 테스트
	// ============================================================

	@Nested
	@DisplayName("채널에 멤버 추가")
	class AddMemberToChannel {

		@Test
		@DisplayName("정상: 활성 채널에 사용자 추가")
		void success_addMember() {
			// Given
			User owner = createUser(UserId.of("owner"));
			Channel channel = channelDomainService.createGroupChannel("Test Channel", owner);
			User newMember = createUser(UserId.of("new-member"));

			// When
			channelDomainService.addMemberToChannel(channel, newMember);

			// Then
			assertThat(channel.isMember(newMember.getId())).isTrue();
		}

		@Test
		@DisplayName("실패: 비활성 채널에 멤버 추가 시도")
		void fail_inactiveChannel() {
			// Given
			User owner = createUser(UserId.of("owner"));
			Channel channel = channelDomainService.createGroupChannel("Test Channel", owner);
			channel.deactivate();
			User newMember = createUser(UserId.of("new-member"));

			// When & Then
			assertThatThrownBy(() -> channelDomainService.addMemberToChannel(channel, newMember))
					.isInstanceOf(DomainException.class)
					.hasMessageContaining("Cannot add member to inactive channel");
		}

		@Test
		@DisplayName("실패: 활성 상태가 아닌 사용자 추가 시도")
		void fail_userNotActive() {
			// Given
			User owner = createUser(UserId.of("owner"));
			Channel channel = channelDomainService.createGroupChannel("Test Channel", owner);
			User bannedUser = createUser(UserId.of("banned-user"));
			bannedUser.ban();

			// When & Then
			assertThatThrownBy(() -> channelDomainService.addMemberToChannel(channel, bannedUser))
					.isInstanceOf(DomainException.class)
					.hasMessageContaining("User is not in active status");
		}

		@Test
		@DisplayName("실패: 이미 멤버인 사용자 추가 시도")
		void fail_alreadyMember() {
			// Given
			User owner = createUser(UserId.of("owner"));
			Channel channel = channelDomainService.createGroupChannel("Test Channel", owner);

			// When & Then
			assertThatThrownBy(() -> channelDomainService.addMemberToChannel(channel, owner))
					.isInstanceOf(DomainException.class)
					.hasMessageContaining("User is already a member of the channel");
		}
	}

	// ============================================================
	// 테스트 헬퍼 메서드
	// ============================================================

	@Nested
	@DisplayName("채널에서 멤버 제거")
	class RemoveMemberFromChannel {

		@Test
		@DisplayName("정상: 채널에서 멤버 제거")
		void success_removeMember() {
			// Given
			User owner = createUser(UserId.of("owner"));
			Channel channel = channelDomainService.createGroupChannel("Test Channel", owner);
			User member = createUser(UserId.of("member"));
			channelDomainService.addMemberToChannel(channel, member);

			// When
			channelDomainService.removeMemberFromChannel(channel, member);

			// Then
			assertThat(channel.isMember(member.getId())).isFalse();
		}

		@Test
		@DisplayName("실패: 채널 소유자 제거 시도")
		void fail_removeOwner() {
			// Given
			User owner = createUser(UserId.of("owner"));
			Channel channel = channelDomainService.createGroupChannel("Test Channel", owner);

			// When & Then
			assertThatThrownBy(() -> channelDomainService.removeMemberFromChannel(channel, owner))
					.isInstanceOf(DomainException.class)
					.hasMessageContaining("Cannot remove channel owner");
		}

		@Test
		@DisplayName("실패: 멤버가 아닌 사용자 제거 시도")
		void fail_notMember() {
			// Given
			User owner = createUser(UserId.of("owner"));
			Channel channel = channelDomainService.createGroupChannel("Test Channel", owner);
			User nonMember = createUser(UserId.of("non-member"));

			// When & Then
			assertThatThrownBy(() -> channelDomainService.removeMemberFromChannel(channel, nonMember))
					.isInstanceOf(DomainException.class)
					.hasMessageContaining("User is not a member of the channel");
		}
	}
}
