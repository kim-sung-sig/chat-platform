package com.example.chat.common.logging;

import org.slf4j.MDC;

import java.util.UUID;

/**
 * MDC(Mapped Diagnostic Context) 유틸리티
 * 로깅 시 컨텍스트 정보를 추적하기 위한 유틸리티
 */
public final class MdcUtil {

    // MDC Keys
    public static final String TRACE_ID = "traceId";
    public static final String SPAN_ID = "spanId";
    public static final String USER_ID = "userId";
    public static final String REQUEST_ID = "requestId";
    public static final String REQUEST_URI = "requestUri";
    public static final String REQUEST_METHOD = "requestMethod";

    private MdcUtil() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    /**
     * Trace ID 생성 및 설정
     */
    public static String generateAndSetTraceId() {
        String traceId = UUID.randomUUID().toString().replace("-", "");
        MDC.put(TRACE_ID, traceId);
        return traceId;
    }

    /**
     * Trace ID 설정 (외부에서 생성된 ID 사용)
     */
    public static void putTraceId(String traceId) {
        if (traceId != null && !traceId.trim().isEmpty()) {
            MDC.put(TRACE_ID, traceId);
        }
    }

    /**
     * Trace ID 제거
     */
    public static void removeTraceId() {
        MDC.remove(TRACE_ID);
    }

    /**
     * Span ID 생성 및 설정
     */
    public static String generateAndSetSpanId() {
        String spanId = UUID.randomUUID().toString().substring(0, 16);
        MDC.put(SPAN_ID, spanId);
        return spanId;
    }

    /**
     * Request ID 생성 및 설정
     */
    public static String generateAndSetRequestId() {
        String requestId = UUID.randomUUID().toString();
        MDC.put(REQUEST_ID, requestId);
        return requestId;
    }

    /**
     * User ID 설정
     */
    public static void setUserId(String userId) {
        if (userId != null && !userId.trim().isEmpty()) {
            MDC.put(USER_ID, userId);
        }
    }

    /**
     * User ID 설정 (Long)
     */
    public static void setUserId(Long userId) {
        if (userId != null) {
            MDC.put(USER_ID, String.valueOf(userId));
        }
    }

    /**
     * Request URI 설정
     */
    public static void setRequestUri(String uri) {
        if (uri != null && !uri.trim().isEmpty()) {
            MDC.put(REQUEST_URI, uri);
        }
    }

    /**
     * Request Method 설정
     */
    public static void setRequestMethod(String method) {
        if (method != null && !method.trim().isEmpty()) {
            MDC.put(REQUEST_METHOD, method);
        }
    }

    /**
     * Trace ID 조회
     */
    public static String getTraceId() {
        return MDC.get(TRACE_ID);
    }

    /**
     * User ID 조회
     */
    public static String getUserId() {
        return MDC.get(USER_ID);
    }

    /**
     * 모든 MDC 컨텍스트 제거
     */
    public static void clear() {
        MDC.clear();
    }

    /**
     * 특정 키의 MDC 값 제거
     */
    public static void remove(String key) {
        MDC.remove(key);
    }
}
