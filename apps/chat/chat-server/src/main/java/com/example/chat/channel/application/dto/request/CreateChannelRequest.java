package com.example.chat.channel.application.dto.request;

import com.example.chat.common.core.enums.ChannelType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 채널 생성 요청 DTO
 *
 * @param name        채널명 (DIRECT 타입 제외)
 * @param description 채널 설명
 * @param type        채널 타입
 * @param otherUserId DIRECT 타입일 때 상대방 userId
 */
public record CreateChannelRequest(
        @Size(max = 100, message = "채널명은 100자 이내여야 합니다")
        String name,

        @Size(max = 500, message = "설명은 500자 이내여야 합니다")
        String description,

        @NotNull(message = "채널 타입은 필수입니다")
        ChannelType type,

        String otherUserId) {
}
