package com.example.chat.message.exception;

import com.example.chat.common.logging.MdcUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * 전역 예외 처리기: 예외 발생 시 MDC의 traceId를 함께 로그에 남기고, 일관된 에러 응답을 반환합니다.
 * TODO: 세부 에러 코드 체계(ErrorCode)를 common 모듈과 연동하고, 발생 원인에 따른 세부 매핑 추가
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception ex) {
        String traceId = MDC.get(MdcUtil.TRACE_ID);
        logger.error("Unhandled exception (traceId={})", traceId, ex);

        Map<String, Object> body = Map.of(
                "timestamp", OffsetDateTime.now().toString(),
                "traceId", traceId,
                "message", ex.getMessage() == null ? "unexpected error" : ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}