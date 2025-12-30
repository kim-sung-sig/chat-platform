package com.example.chat.system.dto.response;

import com.example.chat.domain.channel.Channel;
import com.example.chat.domain.channel.ChannelType;
import com.example.chat.domain.user.UserId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 채널 상세 응답 DTO (멤버 목록 포함)
 */
@Getter
@Builder
@AllArgsConstructor
public class ChannelDetailResponse {

	private String id;
	private String name;
	private String description;
	private ChannelType type;
	private String ownerId;
	private List<String> memberIds;
	private int memberCount;
	private boolean active;
	private Instant createdAt;
	private Instant updatedAt;

	/**
	 * Domain Model → Response DTO 변환
	 */
	public static ChannelDetailResponse from(Channel channel) {
		return ChannelDetailResponse.builder()
				.id(channel.getId().getValue())
				.name(channel.getName())
				.description(channel.getDescription())
				.type(channel.getType())
				.ownerId(channel.getOwnerId().getValue())
				.memberIds(channel.getMemberIds().stream()
						.map(UserId::getValue)
						.collect(Collectors.toList()))
				.memberCount(channel.getMemberCount())
				.active(channel.isActive())
				.createdAt(channel.getCreatedAt())
				.updatedAt(channel.getUpdatedAt())
				.build();
	}
}
