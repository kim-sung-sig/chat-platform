package com.example.chat.common.core.constants;

import lombok.experimental.UtilityClass;

/**
 * HTTP 헤더 상수
 */
@UtilityClass
public final class HeaderConstants {

    // Authorization
    public final String AUTHORIZATION = "Authorization";
    public final String BEARER_PREFIX = "Bearer ";

    // Custom Headers
    public final String X_USER_ID = "X-User-Id";
    public final String X_TENANT_ID = "X-Tenant-Id";
    public final String X_REQUEST_ID = "X-Request-Id";
    public final String X_TRACE_ID = "X-Trace-Id";
    public final String X_SPAN_ID = "X-Span-Id";

    // Content Type
    public final String CONTENT_TYPE = "Content-Type";
    public final String APPLICATION_JSON = "application/json";
    public final String APPLICATION_JSON_UTF8 = "application/json;charset=UTF-8";
}
