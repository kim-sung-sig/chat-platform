package com.example.chat.system.application.service;

import com.example.chat.auth.core.util.SecurityUtils;
import com.example.chat.domain.channel.Channel;
import com.example.chat.domain.channel.ChannelId;
import com.example.chat.domain.channel.ChannelRepository;
import com.example.chat.domain.service.ChannelDomainService;
import com.example.chat.domain.user.User;
import com.example.chat.domain.user.UserId;
import com.example.chat.domain.user.UserRepository;
import com.example.chat.system.dto.request.CreateDirectChannelRequest;
import com.example.chat.system.dto.request.CreateGroupChannelRequest;
import com.example.chat.system.dto.request.CreatePrivateChannelRequest;
import com.example.chat.system.dto.request.CreatePublicChannelRequest;
import com.example.chat.system.dto.request.UpdateChannelRequest;
import com.example.chat.system.dto.response.ChannelDetailResponse;
import com.example.chat.system.dto.response.ChannelResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 채널 애플리케이션 서비스 (Use Case)
 *
 * Application Service의 역할:
 * 1. 트랜잭션 경계 관리
 * 2. 인증/인가 확인
 * 3. Key로 Aggregate 조회
 * 4. Domain Service 호출 (Aggregate 전달)
 * 5. 영속화
 * 6. DTO 변환
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChannelApplicationService {

	private final ChannelRepository channelRepository;
	private final UserRepository userRepository;
	private final ChannelDomainService channelDomainService;

	/**
	 * 일대일 채널 생성 Use Case
	 *
	 * 흐름:
	 * 1. 인증 확인
	 * 2. Aggregate 조회 (User1, User2)
	 * 3. Domain Service 호출 (Channel 생성)
	 * 4. 영속화
	 * 5. DTO 변환
	 */
	@Transactional
	public ChannelResponse createDirectChannel(CreateDirectChannelRequest request) {
		log.info("Creating direct channel: targetUserId={}", request.getTargetUserId());

		// Step 1: Key 조회 - 현재 인증된 사용자 ID
		UserId currentUserId = getUserIdFromContext();

		// Step 2: Key로 Aggregate 조회 - User1 (현재 사용자)
		User user1 = findUserById(currentUserId);

		// Step 3: Key로 Aggregate 조회 - User2 (상대방)
		UserId targetUserId = UserId.of(request.getTargetUserId());
		User user2 = findUserById(targetUserId);

		// Step 4: Domain Service 호출 - Channel 생성 (Aggregate 협력)
		Channel channel = channelDomainService.createDirectChannel(user1, user2);

		// Step 5: 영속화
		Channel savedChannel = channelRepository.save(channel);

		log.info("Direct channel created: channelId={}, user1={}, user2={}",
				savedChannel.getId().getValue(), user1.getId().getValue(), user2.getId().getValue());

		// Step 6: DTO 변환
		return ChannelResponse.from(savedChannel);
	}

	/**
	 * 그룹 채널 생성 Use Case
	 */
	@Transactional
	public ChannelResponse createGroupChannel(CreateGroupChannelRequest request) {
		log.info("Creating group channel: name={}", request.getName());

		// Step 1: Key 조회 - 현재 인증된 사용자 ID
		UserId ownerId = getUserIdFromContext();

		// Step 2: Aggregate 조회 - Owner
		User owner = findUserById(ownerId);

		// Step 3: Domain Service 호출 - Channel 생성
		Channel channel = channelDomainService.createGroupChannel(request.getName(), owner);

		// Step 4: 채널 설명 설정 (선택)
		if (request.getDescription() != null && !request.getDescription().isBlank()) {
			channel.updateInfo(request.getName(), request.getDescription());
		}

		// Step 5: 멤버 추가 (선택)
		if (request.getMemberIds() != null && !request.getMemberIds().isEmpty()) {
			addMembersToChannel(channel, request.getMemberIds());
		}

		// Step 6: 영속화
		Channel savedChannel = channelRepository.save(channel);

		log.info("Group channel created: channelId={}, name={}, ownerId={}",
				savedChannel.getId().getValue(), savedChannel.getName(), owner.getId().getValue());

		// Step 7: DTO 변환
		return ChannelResponse.from(savedChannel);
	}

	/**
	 * 공개 채널 생성 Use Case
	 */
	@Transactional
	public ChannelResponse createPublicChannel(CreatePublicChannelRequest request) {
		log.info("Creating public channel: name={}", request.getName());

		// Step 1: Key 조회
		UserId ownerId = getUserIdFromContext();

		// Step 2: Aggregate 조회
		User owner = findUserById(ownerId);

		// Step 3: Domain Service 호출
		Channel channel = channelDomainService.createPublicChannel(request.getName(), owner);

		// Step 4: 채널 설명 설정
		if (request.getDescription() != null && !request.getDescription().isBlank()) {
			channel.updateInfo(request.getName(), request.getDescription());
		}

		// Step 5: 영속화
		Channel savedChannel = channelRepository.save(channel);

		log.info("Public channel created: channelId={}, name={}",
				savedChannel.getId().getValue(), savedChannel.getName());

		// Step 6: DTO 변환
		return ChannelResponse.from(savedChannel);
	}

	/**
	 * 비공개 채널 생성 Use Case
	 */
	@Transactional
	public ChannelResponse createPrivateChannel(CreatePrivateChannelRequest request) {
		log.info("Creating private channel: name={}", request.getName());

		// Step 1: Key 조회
		UserId ownerId = getUserIdFromContext();

		// Step 2: Aggregate 조회
		User owner = findUserById(ownerId);

		// Step 3: Domain Service 호출
		Channel channel = channelDomainService.createPrivateChannel(request.getName(), owner);

		// Step 4: 채널 설명 설정
		if (request.getDescription() != null && !request.getDescription().isBlank()) {
			channel.updateInfo(request.getName(), request.getDescription());
		}

		// Step 5: 멤버 추가 (필수)
		addMembersToChannel(channel, request.getMemberIds());

		// Step 6: 영속화
		Channel savedChannel = channelRepository.save(channel);

		log.info("Private channel created: channelId={}, name={}",
				savedChannel.getId().getValue(), savedChannel.getName());

		// Step 7: DTO 변환
		return ChannelResponse.from(savedChannel);
	}

	/**
	 * 채널에 멤버 추가 Use Case
	 */
	@Transactional
	public void addMemberToChannel(String channelIdStr, String userIdStr) {
		log.info("Adding member to channel: channelId={}, userId={}", channelIdStr, userIdStr);

		// Step 1: Key로 Aggregate 조회 - Channel
		ChannelId channelId = ChannelId.of(channelIdStr);
		Channel channel = findChannelById(channelId);

		// Step 2: Key로 Aggregate 조회 - User
		UserId userId = UserId.of(userIdStr);
		User user = findUserById(userId);

		// Step 3: Domain Service 호출 (Aggregate 협력)
		channelDomainService.addMemberToChannel(channel, user);

		// Step 4: 영속화
		channelRepository.save(channel);

		log.info("Member added to channel: channelId={}, userId={}", channelIdStr, userIdStr);
	}

	/**
	 * 채널에서 멤버 제거 Use Case
	 */
	@Transactional
	public void removeMemberFromChannel(String channelIdStr, String userIdStr) {
		log.info("Removing member from channel: channelId={}, userId={}", channelIdStr, userIdStr);

		// Step 1: Key로 Aggregate 조회
		ChannelId channelId = ChannelId.of(channelIdStr);
		Channel channel = findChannelById(channelId);

		UserId userId = UserId.of(userIdStr);
		User user = findUserById(userId);

		// Step 2: Domain Service 호출
		channelDomainService.removeMemberFromChannel(channel, user);

		// Step 3: 영속화
		channelRepository.save(channel);

		log.info("Member removed from channel: channelId={}, userId={}", channelIdStr, userIdStr);
	}

	/**
	 * 채널 조회 Use Case
	 */
	public ChannelDetailResponse getChannel(String channelIdStr) {
		log.info("Getting channel: channelId={}", channelIdStr);

		// Key로 Aggregate 조회
		ChannelId channelId = ChannelId.of(channelIdStr);
		Channel channel = findChannelById(channelId);

		// DTO 변환 (상세 정보)
		return ChannelDetailResponse.from(channel);
	}

	/**
	 * 내가 속한 채널 목록 조회 Use Case
	 */
	public List<ChannelResponse> getMyChannels() {
		// Key 조회
		UserId userId = getUserIdFromContext();

		log.info("Getting my channels: userId={}", userId.getValue());

		// Repository 조회
		List<Channel> channels = channelRepository.findByMemberId(userId);

		// DTO 변환
		return channels.stream()
				.map(ChannelResponse::from)
				.collect(Collectors.toList());
	}

	/**
	 * 공개 채널 목록 조회 Use Case
	 */
	public List<ChannelResponse> getPublicChannels() {
		log.info("Getting public channels");

		// Repository 조회
		List<Channel> channels = channelRepository.findPublicChannels();

		// DTO 변환
		return channels.stream()
				.map(ChannelResponse::from)
				.collect(Collectors.toList());
	}

	/**
	 * 채널 정보 수정 Use Case
	 */
	@Transactional
	public ChannelResponse updateChannelInfo(String channelIdStr, UpdateChannelRequest request) {
		log.info("Updating channel info: channelId={}", channelIdStr);

		// Early Return: 수정할 내용이 없는 경우
		if ((request.getName() == null || request.getName().isBlank()) &&
			(request.getDescription() == null)) {
			throw new IllegalArgumentException("At least one field must be provided for update");
		}

		// Key로 Aggregate 조회
		ChannelId channelId = ChannelId.of(channelIdStr);
		Channel channel = findChannelById(channelId);

		// 권한 확인 (소유자만 수정 가능)
		UserId currentUserId = getUserIdFromContext();
		if (!channel.isOwner(currentUserId)) {
			throw new IllegalStateException("Only channel owner can update channel info");
		}

		// Aggregate 메서드 호출
		channel.updateInfo(request.getName(), request.getDescription());

		// 영속화
		Channel updatedChannel = channelRepository.save(channel);

		log.info("Channel info updated: channelId={}", channelIdStr);

		// DTO 변환
		return ChannelResponse.from(updatedChannel);
	}

	/**
	 * 채널 비활성화 Use Case
	 */
	@Transactional
	public void deactivateChannel(String channelIdStr) {
		log.info("Deactivating channel: channelId={}", channelIdStr);

		// Key로 Aggregate 조회
		ChannelId channelId = ChannelId.of(channelIdStr);
		Channel channel = findChannelById(channelId);

		// 권한 확인 (소유자만 비활성화 가능)
		UserId currentUserId = getUserIdFromContext();
		if (!channel.isOwner(currentUserId)) {
			throw new IllegalStateException("Only channel owner can deactivate channel");
		}

		// Aggregate 메서드 호출
		channel.deactivate();

		// 영속화
		channelRepository.save(channel);

		log.info("Channel deactivated: channelId={}", channelIdStr);
	}

	// ============================================================
	// Private Helper Methods
	// ============================================================

	/**
	 * 인증된 사용자 ID 조회
	 */
	private UserId getUserIdFromContext() {
		String userIdStr = SecurityUtils.getCurrentUserId()
				.orElseThrow(() -> new IllegalStateException("User not authenticated"));

		return UserId.of(userIdStr);
	}

	/**
	 * Channel Aggregate 조회
	 */
	private Channel findChannelById(ChannelId channelId) {
		return channelRepository.findById(channelId)
				.orElseThrow(() -> new IllegalArgumentException(
						"Channel not found: " + channelId.getValue()
				));
	}

	/**
	 * User Aggregate 조회
	 */
	private User findUserById(UserId userId) {
		return userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException(
						"User not found: " + userId.getValue()
				));
	}

	/**
	 * 채널에 여러 멤버 추가
	 */
	private void addMembersToChannel(Channel channel, List<String> userIds) {
		// Early Return: 멤버 목록이 비어있는 경우
		if (userIds == null || userIds.isEmpty()) {
			return;
		}

		for (String userIdStr : userIds) {
			UserId userId = UserId.of(userIdStr);
			User user = findUserById(userId);
			channelDomainService.addMemberToChannel(channel, user);
		}
	}
}
