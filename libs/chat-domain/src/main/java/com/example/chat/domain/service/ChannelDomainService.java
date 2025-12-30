package com.example.chat.domain.service;

import com.example.chat.domain.channel.Channel;
import com.example.chat.domain.channel.ChannelType;
import com.example.chat.domain.user.User;
import com.example.chat.domain.user.UserId;

/**
 * 채널 도메인 서비스
 *
 * DDD Domain Service의 역할:
 * - Channel Aggregate 생성 시 복잡한 도메인 규칙 검증
 * - User + Channel Aggregate 간 협력 조율
 */
public class ChannelDomainService {

	/**
	 * 일대일 채팅 채널 생성
	 *
	 * Domain Rule:
	 * - 두 사용자는 서로 다른 사용자여야 함
	 * - 두 사용자 모두 활성 상태여야 함
	 *
	 * @param user1 첫 번째 사용자 (Aggregate Root)
	 * @param user2 두 번째 사용자 (Aggregate Root)
	 * @return 생성된 채널 (Aggregate Root)
	 */
	public Channel createDirectChannel(User user1, User user2) {
		// Early Return: 동일 사용자 체크
		if (user1.getId().equals(user2.getId())) {
			throw new DomainException("Cannot create direct channel with same user");
		}

		// Early Return: 사용자 활성 상태 검증
		if (!user1.canSendMessage()) {
			throw new DomainException("User1 is not in active status");
		}
		if (!user2.canSendMessage()) {
			throw new DomainException("User2 is not in active status");
		}

		// 채널 생성
		String channelName = generateDirectChannelName(user1.getId(), user2.getId());
		Channel channel = Channel.create(channelName, ChannelType.DIRECT, user1.getId());
		channel.addMember(user2.getId());

		return channel;
	}

	/**
	 * 그룹 채팅 채널 생성
	 *
	 * @param name 채널명
	 * @param owner 채널 소유자 (Aggregate Root)
	 */
	public Channel createGroupChannel(String name, User owner) {
		// Early Return: 입력값 검증
		validateChannelName(name);

		// Early Return: 소유자 상태 검증
		if (!owner.canSendMessage()) {
			throw new DomainException("Owner is not in active status");
		}

		return Channel.create(name, ChannelType.GROUP, owner.getId());
	}

	/**
	 * 공개 채널 생성
	 */
	public Channel createPublicChannel(String name, User owner) {
		// Early Return: 입력값 검증
		validateChannelName(name);

		// Early Return: 소유자 상태 검증
		if (!owner.canSendMessage()) {
			throw new DomainException("Owner is not in active status");
		}

		return Channel.create(name, ChannelType.PUBLIC, owner.getId());
	}

	/**
	 * 비공개 채널 생성
	 */
	public Channel createPrivateChannel(String name, User owner) {
		// Early Return: 입력값 검증
		validateChannelName(name);

		// Early Return: 소유자 상태 검증
		if (!owner.canSendMessage()) {
			throw new DomainException("Owner is not in active status");
		}

		return Channel.create(name, ChannelType.PRIVATE, owner.getId());
	}

	/**
	 * 채널에 멤버 추가
	 *
	 * Domain Rule:
	 * - 채널은 활성 상태여야 함
	 * - 사용자는 활성 상태여야 함
	 * - 이미 멤버가 아니어야 함
	 */
	public void addMemberToChannel(Channel channel, User user) {
		// Early Return: 채널 상태 검증
		if (!channel.isActive()) {
			throw new DomainException("Cannot add member to inactive channel");
		}

		// Early Return: 사용자 상태 검증
		if (!user.canSendMessage()) {
			throw new DomainException("User is not in active status");
		}

		// Early Return: 중복 멤버 체크
		if (channel.isMember(user.getId())) {
			throw new DomainException("User is already a member of the channel");
		}

		// 멤버 추가
		channel.addMember(user.getId());
	}

	/**
	 * 채널에서 멤버 제거
	 *
	 * Domain Rule:
	 * - 채널 소유자는 제거할 수 없음
	 * - 멤버여야 제거 가능
	 */
	public void removeMemberFromChannel(Channel channel, User user) {
		// Early Return: 소유자 제거 방지
		if (channel.isOwner(user.getId())) {
			throw new DomainException("Cannot remove channel owner");
		}

		// Early Return: 멤버 여부 체크
		if (!channel.isMember(user.getId())) {
			throw new DomainException("User is not a member of the channel");
		}

		// 멤버 제거
		channel.removeMember(user.getId());
	}

	// ============================================================
	// 입력값 검증 메서드
	// ============================================================

	/**
	 * 채널명 검증
	 */
	private void validateChannelName(String name) {
		// Early Return: null/blank 체크
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("Channel name cannot be null or blank");
		}

		// Early Return: 길이 제한 체크
		if (name.length() > 100) {
			throw new IllegalArgumentException("Channel name exceeds maximum length (100)");
		}
	}

	/**
	 * 일대일 채팅 채널명 생성
	 */
	private String generateDirectChannelName(UserId user1, UserId user2) {
		return "direct_" + user1.getValue() + "_" + user2.getValue();
	}
}
