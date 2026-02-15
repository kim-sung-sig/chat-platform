package com.example.chat.message.presentation.controller.test

import com.example.chat.auth.core.annotation.CurrentUser
import com.example.chat.auth.core.model.AuthenticatedUser
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 테스트용 컨트롤러
 *
 * JWT 인증 및 사용자 정보 조회 테스트
 */
@RestController
@RequestMapping("/api/test")
class TestController {

	@GetMapping("/hello")
	fun hello(@CurrentUser user: AuthenticatedUser): String {
		return "Hello, ${user.userId}!"
	}
}
