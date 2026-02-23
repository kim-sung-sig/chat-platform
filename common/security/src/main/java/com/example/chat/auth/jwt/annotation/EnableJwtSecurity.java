package com.example.chat.auth.jwt.annotation;

import com.example.chat.auth.jwt.config.JwtSecurityAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * JWT 보안 설정을 활성화하는 어노테이션
 * JWT 인증 필터 및 보안 설정이 자동으로 구성됩니다.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(JwtSecurityAutoConfiguration.class)
public @interface EnableJwtSecurity {
}
