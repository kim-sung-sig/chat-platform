package com.example.chat.system.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 일대일 채널 생성 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDirectChannelRequest {

	/**
	 * 상대방 사용자 ID
	 * (현재 로그인한 사용자는 컨텍스트에서 가져옴)
	 */
	@NotBlank(message = "Target user ID is required")
	private String targetUserId;
}
