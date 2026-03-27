package com.example.chat.channel.application.service;

import java.time.Instant;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.chat.auth.core.util.SecurityUtils;
import com.example.chat.shared.cache.UnreadCacheService;
import com.example.chat.channel.rest.dto.request.CreateChannelRequest;
import com.example.chat.channel.rest.dto.response.ChannelResponse;
import com.example.chat.common.core.enums.ChannelType;
import com.example.chat.common.core.exception.ChatErrorCode;
import com.example.chat.shared.exception.ChatException;
import com.example.chat.shared.exception.ResourceNotFoundException;
import com.example.chat.message.infrastructure.kafka.KafkaMessageProducer;
import com.example.chat.storage.domain.entity.ChatChannelEntity;
import com.example.chat.storage.domain.entity.ChatChannelMemberEntity;
import com.example.chat.storage.domain.entity.UserEntity;
import com.example.chat.storage.domain.repository.JpaChannelMemberRepository;
import com.example.chat.storage.domain.repository.JpaChannelMetadataRepository;
import com.example.chat.storage.domain.repository.JpaChannelRepository;
import com.example.chat.storage.domain.repository.JpaUserRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * 채널 생성/수정/삭제 Command Service
 * <p>
 * 책임:
 * - 채널 생성 (DIRECT / GROUP / PUBLIC / PRIVATE)
 * - 채널 비활성화(삭제)
 * - 채널 멤버 추가/제거
 * <p>
 * SRP: 채널 상태 변경 전담
 * DIP: JPA Repository 직접 주입 (Adapter 제거됨)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChannelCommandService {

	private final JpaChannelRepository channelRepository;
	private final JpaChannelMemberRepository channelMemberRepository;
	private final JpaUserRepository userRepository;
	private final JpaChannelMetadataRepository channelMetadataRepository;
	private final KafkaMessageProducer kafkaMessageProducer;
	private final UnreadCacheService unreadCacheService;

	/**
	 * 채널 생성
	 */
	@Transactional
	public ChannelResponse createChannel(CreateChannelRequest request) {
		String currentUserId = requireCurrentUserId();
		log.info("Creating channel: type={}, userId={}", request.type(), currentUserId);

		UserEntity owner = findUser(currentUserId);

		ChatChannelEntity channel = buildChannel(request, owner);
		ChatChannelEntity saved = channelRepository.save(channel);

		// 생성자를 멤버로 추가
		channelMemberRepository.save(buildMember(saved.getId(), currentUserId));

		// DIRECT 채널이면 상대방도 멤버로 추가
		if (request.type() == ChannelType.DIRECT) {
			String otherId = requireOtherUserId(request);
			validateDifferentUsers(currentUserId, otherId);
			findUser(otherId); // 존재 확인
			channelMemberRepository.save(buildMember(saved.getId(), otherId));
		}

		log.info("Channel created: channelId={}", saved.getId());
		return ChannelResponse.fromEntity(saved);
	}

	/**
	 * 채널 비활성화 (소프트 삭제)
	 */
	@Transactional
	public void deactivateChannel(String channelId) {
		String currentUserId = requireCurrentUserId();
		log.info("Deactivating channel: channelId={}, userId={}", channelId, currentUserId);

		ChatChannelEntity channel = findChannel(channelId);
		validateOwner(channel, currentUserId);
		channel.deactivate();
		channelRepository.save(channel);
	}

	/**
	 * 채널 멤버 추가
	 */
	@Transactional
	public void addMember(String channelId, String targetUserId) {
		String currentUserId = requireCurrentUserId();
		log.info("Adding member: channelId={}, targetUserId={}", channelId, targetUserId);

		ChatChannelEntity channel = findChannel(channelId);
		if (!channel.isActive()) throw new ChatException(ChatErrorCode.CHANNEL_NOT_ACTIVE);
		validateOwner(channel, currentUserId);
		findUser(targetUserId); // 존재 확인

		if (channelMemberRepository.existsByChannelIdAndUserId(channelId, targetUserId)) {
			throw new ChatException(ChatErrorCode.CHANNEL_ALREADY_MEMBER);
		}
		channelMemberRepository.save(buildMember(channelId, targetUserId));
	}

	/**
	 * 채널 멤버 제거
	 */
	@Transactional
	public void removeMember(String channelId, String targetUserId) {
		String currentUserId = requireCurrentUserId();
		log.info("Removing member: channelId={}, targetUserId={}", channelId, targetUserId);

		ChatChannelEntity channel = findChannel(channelId);
		// 소유자거나 자기 자신만 제거 가능
		boolean isSelf = currentUserId.equals(targetUserId);
		boolean isOwner = channel.getOwnerId().equals(currentUserId);
		if (!isSelf && !isOwner) throw new ChatException(ChatErrorCode.CHANNEL_NOT_MEMBER);
		// 소유자 자신은 제거 불가
		if (channel.getOwnerId().equals(targetUserId)) {
			throw new ChatException(ChatErrorCode.DOMAIN_RULE_VIOLATION);
		}

		// 퇴장 전: 마지막 읽음 미마지 (Kafka Consumer가 unread_count 보정에 사용)
		Instant lastReadAt = channelMetadataRepository
				.findByChannelIdAndUserId(channelId, targetUserId)
				.map(m -> m.getLastReadAt())
				.orElse(null);

		channelMemberRepository.deleteByChannelIdAndUserId(channelId, targetUserId);

		// Redis Cache: 퇴장 사용자 unread 필드 즉시 제거 (Phase 9)
		unreadCacheService.evictUser(channelId, targetUserId);

		// Kafka 비동기: 퇴장 멤버의 미읽음 message.unread_count 보정
		// (channelMetadataRepository.deleteByChannelIdAndUserId는 Consumer에서 처리)
		kafkaMessageProducer.publishMemberLeft(targetUserId, channelId, lastReadAt);
	}

	// =============================================
	// Private Helpers
	// =============================================

	private ChatChannelEntity buildChannel(CreateChannelRequest request, UserEntity owner) {
		String channelName = resolveChannelName(request);
		return ChatChannelEntity.create(
				UUID.randomUUID().toString(),
				channelName,
				request.description(),
				request.type(),
				owner.getId());
	}

	private String resolveChannelName(CreateChannelRequest request) {
		if (request.type() == ChannelType.DIRECT) {
			return "direct_" + request.otherUserId();
		}
		if (request.name() == null || request.name().isBlank()) {
			throw new ChatException(ChatErrorCode.DOMAIN_RULE_VIOLATION);
		}
		return request.name();
	}

	private ChatChannelMemberEntity buildMember(String channelId, String userId) {
		return ChatChannelMemberEntity.create(channelId, userId);
	}

	private ChatChannelEntity findChannel(String channelId) {
		return channelRepository.findById(channelId)
				.orElseThrow(() -> new ResourceNotFoundException(ChatErrorCode.CHANNEL_NOT_FOUND));
	}

	private UserEntity findUser(String userId) {
		return userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException(ChatErrorCode.USER_NOT_FOUND));
	}

	private String requireCurrentUserId() {
		return SecurityUtils.getCurrentUserId()
				.orElseThrow(() -> new IllegalStateException("User not authenticated"));
	}

	private String requireOtherUserId(CreateChannelRequest request) {
		if (request.otherUserId() == null || request.otherUserId().isBlank()) {
			throw new ChatException(ChatErrorCode.DOMAIN_RULE_VIOLATION);
		}
		return request.otherUserId();
	}

	private void validateOwner(ChatChannelEntity channel, String userId) {
		if (!channel.getOwnerId().equals(userId)) {
			throw new ChatException(ChatErrorCode.CHANNEL_NOT_MEMBER);
		}
	}

	private void validateDifferentUsers(String userId1, String userId2) {
		if (userId1.equals(userId2)) {
			throw new ChatException(ChatErrorCode.DOMAIN_RULE_VIOLATION);
		}
	}
}
