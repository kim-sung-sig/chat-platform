package com.example.chat.message.presentation.controller.test;

import com.example.chat.auth.core.model.AuthenticatedUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 테스트용 컨트롤러
 *
 * JWT 인증 및 사용자 정보 조회 테스트
 */
@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/hello")
    public String hello(@AuthenticationPrincipal AuthenticatedUser user) {
        return "Hello, " + user.userId() + "!";
    }
}
