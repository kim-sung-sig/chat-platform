package com.example.chat.system.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 비공개 채널 생성 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePrivateChannelRequest {

	/**
	 * 채널명
	 */
	@NotBlank(message = "Channel name is required")
	@Size(max = 100, message = "Channel name cannot exceed 100 characters")
	private String name;

	/**
	 * 채널 설명 (선택)
	 */
	@Size(max = 500, message = "Description cannot exceed 500 characters")
	private String description;

	/**
	 * 초대할 멤버 ID 목록 (필수 - 비공개 채널은 초대된 사람만 참여 가능)
	 */
	@NotEmpty(message = "At least one member is required for private channel")
	private List<String> memberIds;
}
