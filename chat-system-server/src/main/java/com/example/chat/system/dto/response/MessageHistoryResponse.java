package com.example.chat.system.dto.response;

import com.example.chat.system.domain.entity.MessageHistory;
import com.example.chat.system.domain.enums.PublishStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 메시지 발행 이력 응답 DTO (커서 기반 페이징용)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageHistoryResponse {

    private Long id; // 커서로 사용
    private Long messageId;
    private String messageTitle;
    private Long customerId;
    private String customerName;
    private PublishStatus publishStatus;
    private LocalDateTime publishedAt;
    private String errorMessage;
    private Integer retryCount;
    private LocalDateTime createdAt;

    /**
     * Entity -> DTO 변환
     */
    public static MessageHistoryResponse from(MessageHistory history) {
        return MessageHistoryResponse.builder()
                .id(history.getId())
                .messageId(history.getMessage().getId())
                .messageTitle(history.getMessage().getTitle())
                .customerId(history.getCustomer().getId())
                .customerName(history.getCustomer().getCustomerName())
                .publishStatus(history.getPublishStatus())
                .publishedAt(history.getPublishedAt())
                .errorMessage(history.getErrorMessage())
                .retryCount(history.getRetryCount())
                .createdAt(history.getCreatedAt())
                .build();
    }
}