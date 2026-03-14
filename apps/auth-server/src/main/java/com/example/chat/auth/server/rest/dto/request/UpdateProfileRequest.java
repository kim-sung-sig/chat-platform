package com.example.chat.auth.server.rest.dto.request;

import jakarta.validation.constraints.Size;

/**
 * 프로필 수정 요청
 */
public record UpdateProfileRequest(
        @Size(min = 1, max = 50, message = "닉네임은 1~50자 사이여야 합니다")
        String nickname,

        @Size(max = 500, message = "아바타 URL은 500자를 초과할 수 없습니다")
        String avatarUrl,

        @Size(max = 20, message = "전화번호는 20자를 초과할 수 없습니다")
        String phoneNumber,

        @Size(max = 200, message = "자기소개는 200자를 초과할 수 없습니다")
        String bio
) {
}
