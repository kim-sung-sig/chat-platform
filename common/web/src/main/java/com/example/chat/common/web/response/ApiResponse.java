package com.example.chat.common.web.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.ResponseEntity;

/**
 * 공통 API 응답 DTO
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        int status,
        String message,
        T data) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(200, "OK", data);
    }

    public static ApiResponse<Void> ok() {
        return new ApiResponse<>(200, "OK", null);
    }

    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>(201, "Created", data);
    }

    public static ApiResponse<Void> created() {
        return new ApiResponse<>(201, "Created", null);
    }

    public static ApiResponse<Void> noContent() {
        return new ApiResponse<>(204, "No Content", null);
    }

    public ResponseEntity<ApiResponse<T>> toResponseEntity() {
        return ResponseEntity.status(status).body(this);
    }
}
