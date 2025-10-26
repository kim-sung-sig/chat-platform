package com.example.chat.system.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 채널 생성 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChannelCreateRequest {

    @NotNull(message = "채널명은 필수입니다")
    private String channelName;

    @NotNull(message = "채널 타입은 필수입니다")
    private String channelType;

    private String description;

    @NotNull(message = "소유자 ID는 필수입니다")
    private Long ownerId;
}