package com.example.chat.system.dto.request;

import com.example.chat.system.domain.enums.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 메시지 생성 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageCreateRequest {

    @NotNull(message = "채널 ID는 필수입니다")
    private Long channelId;

    @NotBlank(message = "제목은 필수입니다")
    private String title;

    @NotBlank(message = "내용은 필수입니다")
    private String content;

    @NotNull(message = "메시지 타입은 필수입니다")
    private MessageType messageType;

    @NotNull(message = "작성자 ID는 필수입니다")
    private Long createdBy;
}