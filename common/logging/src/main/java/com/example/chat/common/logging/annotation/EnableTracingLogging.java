package com.example.chat.common.logging.annotation;

import com.example.chat.common.logging.config.TracingLoggingRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Micrometer Tracing 기반 로깅을 활성화한다.
 *
 * <p>사용법: 각 서비스의 Application 클래스에 선언한다.
 * <pre>
 * {@code
 * @SpringBootApplication
 * @EnableTracingLogging
 * public class AuthServerApplication { ... }
 * }
 * </pre>
 *
 * <p>활성화 시 제공되는 기능:
 * <ul>
 *   <li>Servlet 환경 → {@code TracingFilter} 자동 등록 (요청/응답 로깅 + X-Trace-Id 응답 헤더)</li>
 *   <li>Reactive 환경 → {@code ReactiveTracingFilter} 자동 등록</li>
 * </ul>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(TracingLoggingRegistrar.class)
public @interface EnableTracingLogging {
}
