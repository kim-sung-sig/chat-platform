package com.example.chat.message.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
@RequiredArgsConstructor
public class MessageSendFacade {

	private final MessageSendValidator messageSendValidator;

	private final MessageService messageService;

	public void sendMessage(Object messageRequest, Principal principal) {

		// 메시지 유효성 검증
		messageSendValidator.validate(messageRequest, principal);

		// 메시지 전송
		messageService.sendMessage(messageRequest);
	}
}