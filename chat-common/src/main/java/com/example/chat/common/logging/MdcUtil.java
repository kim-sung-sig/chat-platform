package com.example.chat.common.logging;

import org.slf4j.MDC;

/**
 * 작은 MDC 유틸리티. 프로젝트 전반에서 traceId 같은 공통 MDC 키를 관리합니다.
 * TODO: 필요하다면 UUID 생성 정책, 헤더 이름 상수화, Sleuth/Zipkin 연동으로 확장하세요.
 */
public final class MdcUtil {
    public static final String TRACE_ID = "traceId";

    private MdcUtil() {}

    public static void putTraceId(String traceId) {
        if (traceId != null) MDC.put(TRACE_ID, traceId);
    }

    public static void removeTraceId() {
        MDC.remove(TRACE_ID);
    }

    public static void clear() {
        MDC.clear();
    }
}