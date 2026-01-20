package com.example.chat.common.web.response;

import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
    int status,
    String message,
    T data
) {
    public ApiResponse {
        if (!StringUtils.hasText(message)) {
            message = switch (status) {
                case 200 -> "OK";
                case 201 -> "Created";
                default -> "Unknown Status";
            };
        }
    }

    public static ApiResponse<Void> ok() {
        return ok(null);
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(
            200,
            "OK",
            data
        );
    }
}
