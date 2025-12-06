package com.example.chat.common.util.constants;

/**
 * HTTP 헤더 상수
 */
public final class HeaderConstants {

    private HeaderConstants() {
        throw new AssertionError("Cannot instantiate constants class");
    }

    // Authorization
    public static final String AUTHORIZATION = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";

    // Custom Headers
    public static final String X_USER_ID = "X-User-Id";
    public static final String X_TENANT_ID = "X-Tenant-Id";
    public static final String X_REQUEST_ID = "X-Request-Id";
    public static final String X_TRACE_ID = "X-Trace-Id";
    public static final String X_SPAN_ID = "X-Span-Id";

    // Content Type
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String APPLICATION_JSON = "application/json";
    public static final String APPLICATION_JSON_UTF8 = "application/json;charset=UTF-8";
}
