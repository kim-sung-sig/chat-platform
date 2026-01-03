package com.example.chat.auth.core.annotation;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 현재 인증된 사용자를 주입받는 어노테이션
 *
 * 사용 예:
 * <pre>
 * {@code
 * @GetMapping("/me")
 * public ResponseEntity<UserInfo> getCurrentUser(@CurrentUser AuthenticatedUser user) {
 *     return ResponseEntity.ok(userService.getUserInfo(user.getUserId()));
 * }
 * }
 * </pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@AuthenticationPrincipal(expression = "@authConverter.convert(#this)")
public @interface CurrentUser {
}

