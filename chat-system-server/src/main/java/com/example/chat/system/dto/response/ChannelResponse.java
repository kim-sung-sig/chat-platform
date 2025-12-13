package com.example.chat.system.dto.response;

import com.example.chat.domain.channel.Channel;
import com.example.chat.domain.channel.ChannelType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * 채널 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
public class ChannelResponse {

	private String id;
	private String name;
	private String description;
	private ChannelType type;
	private String ownerId;
	private int memberCount;
	private boolean active;
	private Instant createdAt;
	private Instant updatedAt;

	/**
	 * Domain Model → Response DTO 변환
	 */
	public static ChannelResponse from(Channel channel) {
		return ChannelResponse.builder()
				.id(channel.getId().getValue())
				.name(channel.getName())
				.description(channel.getDescription())
				.type(channel.getType())
				.ownerId(channel.getOwnerId().getValue())
				.memberCount(channel.getMemberCount())
				.active(channel.isActive())
				.createdAt(channel.getCreatedAt())
				.updatedAt(channel.getUpdatedAt())
				.build();
	}
}
