package com.example.chat.message.controller;

import com.example.chat.message.service.MessageSendFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@Slf4j
@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MessageController {

	private final MessageSendFacade messageSendFacade;

	@PostMapping("/send")
	public void sendMessage(
			@AuthenticationPrincipal Principal principal,
			@RequestBody Object messageSendRequest
	) {
		messageSendFacade.sendMessage(messageSendRequest, principal);
	}

}