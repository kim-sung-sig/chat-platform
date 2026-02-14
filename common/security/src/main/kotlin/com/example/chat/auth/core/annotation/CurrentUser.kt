package com.example.chat.auth.core.annotation

import org.springframework.security.core.annotation.AuthenticationPrincipal

/**
 * 현재 인증된 사용자를 주입받는 어노테이션
 *
 * 사용 예:
 * ```kotlin
 * @GetMapping("/me")
 * fun getCurrentUser(@CurrentUser user: AuthenticatedUser): ResponseEntity<UserInfo> {
 *     return ResponseEntity.ok(userService.getUserInfo(user.userId))
 * }
 * ```
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@AuthenticationPrincipal(expression = "@authConverter.convert(#this)")
annotation class CurrentUser
