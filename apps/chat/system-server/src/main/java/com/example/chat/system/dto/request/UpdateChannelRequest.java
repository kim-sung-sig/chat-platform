package com.example.chat.system.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 채널 정보 수정 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateChannelRequest {

	/**
	 * 채널명 (선택)
	 */
	@Size(max = 100, message = "Channel name cannot exceed 100 characters")
	private String name;

	/**
	 * 채널 설명 (선택)
	 */
	@Size(max = 500, message = "Description cannot exceed 500 characters")
	private String description;
}
