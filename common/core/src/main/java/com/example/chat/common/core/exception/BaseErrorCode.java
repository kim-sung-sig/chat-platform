package com.example.chat.common.core.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 기본 에러 코드
 * 시스템 전반에서 공통으로 사용되는 에러 코드 정의
 */
@Getter
@AllArgsConstructor
public enum BaseErrorCode implements ErrorCode {

	// 2xx Success
	SUCCESS("CMN-200", "요청이 성공적으로 처리되었습니다", 200),
	CREATED("CMN-201", "리소스가 성공적으로 생성되었습니다", 201),
	NO_CONTENT("CMN-204", "처리할 내용이 없습니다", 204),

	// 4xx Client Errors
	BAD_REQUEST("CMN-400", "잘못된 요청입니다", 400),
	UNAUTHORIZED("CMN-401", "인증이 필요합니다", 401),
	FORBIDDEN("CMN-403", "접근 권한이 없습니다", 403),
	NOT_FOUND("CMN-404", "리소스를 찾을 수 없습니다", 404),
	METHOD_NOT_ALLOWED("CMN-405", "허용되지 않은 메서드입니다", 405),
	UNSUPPORTED_MEDIA_TYPE("CMN-415", "지원하지 않는 Content-Type 입니다", 415),
	CONFLICT("CMN-409", "리소스 충돌이 발생했습니다", 409),

	// 5xx Server Errors
	INTERNAL_SERVER_ERROR("CMN-500", "서버 내부 오류가 발생했습니다", 500),
	SERVICE_UNAVAILABLE("CMN-503", "서비스를 사용할 수 없습니다", 503),

	// Validation Errors
	VALIDATION_ERROR("CMN-VAL-001", "입력값 검증에 실패했습니다", 400),

	// Business Logic Errors
	BUSINESS_LOGIC_ERROR("CMN-BIZ-001", "비즈니스 로직 오류가 발생했습니다", 400);

	private final String code;
	private final String message;
	private final int status;
}
