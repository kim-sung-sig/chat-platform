package com.example.chat.system.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 그룹 채널 생성 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateGroupChannelRequest {

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
	 * 초대할 멤버 ID 목록 (선택)
	 */
	private List<String> memberIds;
}
