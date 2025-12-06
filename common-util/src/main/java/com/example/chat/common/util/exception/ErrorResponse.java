package com.example.chat.common.util.exception;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 에러 응답 DTO
 * 클라이언트에게 일관된 형식의 에러 응답 제공
 */
@Getter
@Builder
public class ErrorResponse {

    private final String code;
    private final String message;
    private final int status;
    private final LocalDateTime timestamp;
    private final String path;
    private final List<FieldError> fieldErrors;

    /**
     * 필드 에러 DTO
     */
    @Getter
    @Builder
    public static class FieldError {
        private final String field;
        private final String rejectedValue;
        private final String message;
    }

    /**
     * BaseException으로부터 ErrorResponse 생성
     */
    public static ErrorResponse of(BaseException ex, String path) {
        return ErrorResponse.builder()
                .code(ex.getCode())
                .message(ex.getMessage())
                .status(ex.getStatus())
                .timestamp(LocalDateTime.now())
                .path(path)
                .build();
    }

    /**
     * ErrorCode로부터 ErrorResponse 생성
     */
    public static ErrorResponse of(ErrorCode errorCode, String path) {
        return ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .status(errorCode.getStatus())
                .timestamp(LocalDateTime.now())
                .path(path)
                .build();
    }

    /**
     * ErrorResponse를 ResponseEntity로 변환
     */
    public ResponseEntity<ErrorResponse> toResponseEntity() {
        return ResponseEntity
                .status(this.status)
                .body(this);
    }
}
