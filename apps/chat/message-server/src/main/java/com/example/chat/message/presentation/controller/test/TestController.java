package com.example.chat.message.presentation.controller.test;

import com.example.chat.auth.core.annotation.CurrentUser;
import com.example.chat.auth.core.model.AuthenticatedUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

	@GetMapping("/hello")
	public String hello(
			@CurrentUser AuthenticatedUser user
	) {
		return "Hello, " + user.getUserId() + "!";
	}
}
