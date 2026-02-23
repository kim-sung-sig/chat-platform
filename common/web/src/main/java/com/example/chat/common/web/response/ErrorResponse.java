package com.example.chat.common.web.response;

import com.example.chat.common.core.exception.BaseException;
import com.example.chat.common.core.exception.BaseErrorCode;
import com.example.chat.common.core.exception.ErrorCode;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * 에러 응답 DTO
 * 클라이언트에게 일관된 형식의 에러 응답 제공
 */
public record ErrorResponse(
        int status,
        String code,
        String message,
        List<FieldError> fieldErrors) {

    public ErrorResponse(int status, String code, String message) {
        this(status, code, message, List.of());
    }

    public ResponseEntity<ErrorResponse> toResponseEntity() {
        return ResponseEntity.status(status).body(this);
    }

    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.getStatus(), errorCode.getCode(), errorCode.getMessage());
    }

    public static ErrorResponse of(BaseException ex) {
        return of(ex.getErrorCode());
    }

    public static ErrorResponse of(List<FieldError> fieldErrors) {
        BaseErrorCode errorCode = BaseErrorCode.VALIDATION_ERROR;
        return new ErrorResponse(errorCode.getStatus(), errorCode.getCode(), errorCode.getMessage(), fieldErrors);
    }

    /**
     * 필드 에러 DTO
     */
    public record FieldError(
            String field,
            String rejectedValue,
            String message) {
    }
}
