package com.example.chat.message.presentation.controller;

import com.example.chat.domain.message.MessageType;
import com.example.chat.message.application.dto.request.SendMessageRequest;
import com.example.chat.message.application.dto.response.MessageResponse;
import com.example.chat.message.test.AbstractIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MessageController 통합 테스트
 * TestContainers를 사용한 실제 환경 테스트
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("MessageController 통합 테스트")
class MessageControllerIntegrationTest extends AbstractIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	@DisplayName("Health Check - 서버 정상 상태 확인")
	void healthCheck() throws Exception {
		// When & Then
		mockMvc.perform(get("/api/messages/health"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(content().string("OK"));
	}

	@Test
	@WithMockUser(username = "testUser")
	@DisplayName("메시지 발송 성공 - 텍스트 메시지")
	void sendMessage_Success_TextMessage() throws Exception {
		// Given
		SendMessageRequest request = SendMessageRequest.builder()
				.channelId("channel-456")
				.messageType(MessageType.TEXT)
				.payload(Map.of("text", "안녕하세요!"))
				.build();

		// When & Then
		mockMvc.perform(post("/api/messages")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.channelId").value("channel-456"))
				.andExpect(jsonPath("$.messageType").value("TEXT"))
				.andExpect(jsonPath("$.status").exists())
				.andExpect(jsonPath("$.sentAt").exists());
	}

	@Test
	@WithMockUser(username = "testUser")
	@DisplayName("메시지 발송 성공 - 이미지 메시지")
	void sendMessage_Success_ImageMessage() throws Exception {
		// Given
		SendMessageRequest request = SendMessageRequest.builder()
				.channelId("channel-456")
				.messageType(MessageType.IMAGE)
				.payload(Map.of(
						"imageUrl", "https://example.com/image.jpg",
						"width", 1024,
						"height", 768
				))
				.build();

		// When & Then
		mockMvc.perform(post("/api/messages")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.messageType").value("IMAGE"));
	}

	@Test
	@WithMockUser(username = "testUser")
	@DisplayName("답장 메시지 발송 - replyToMessageId 포함")
	void sendReplyMessage_Success() throws Exception {
		// Given: 먼저 원본 메시지 발송
		SendMessageRequest originalRequest = SendMessageRequest.builder()
				.channelId("channel-456")
				.messageType(MessageType.TEXT)
				.payload(Map.of("text", "원본 메시지"))
				.build();

		String originalResponse = mockMvc.perform(post("/api/messages")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(originalRequest)))
				.andReturn()
				.getResponse()
				.getContentAsString();

		MessageResponse originalMessage = objectMapper.readValue(originalResponse, MessageResponse.class);

		// Given: 답장 메시지 요청
		SendMessageRequest replyRequest = SendMessageRequest.builder()
				.channelId("channel-456")
				.messageType(MessageType.TEXT)
				.payload(Map.of("text", "답장입니다"))
				.build();

		// When & Then
		mockMvc.perform(post("/api/messages/reply")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(replyRequest)))
				.andDo(print())
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.replyToMessageId").value(originalMessage.getId()));
	}

	@Test
	@WithMockUser(username = "testUser")
	@DisplayName("메시지 발송 실패 - roomId 누락 (Validation)")
	void sendMessage_Fail_MissingRoomId() throws Exception {
		// Given: roomId 없는 요청
		SendMessageRequest request = SendMessageRequest.builder()
				.channelId("channel-456")
				.messageType(MessageType.TEXT)
				.payload(Map.of("text", "안녕하세요!"))
				.build();

		// When & Then
		mockMvc.perform(post("/api/messages")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isBadRequest());
	}

	@Test
	@WithMockUser(username = "testUser")
	@DisplayName("메시지 발송 실패 - messageType 누락 (Validation)")
	void sendMessage_Fail_MissingMessageType() throws Exception {
		// Given: messageType 없는 요청
		SendMessageRequest request = SendMessageRequest.builder()
				.channelId("channel-456")
				.payload(Map.of("text", "안녕하세요!"))
				.build();

		// When & Then
		mockMvc.perform(post("/api/messages")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("메시지 발송 실패 - 인증 없음 (401 Unauthorized)")
	void sendMessage_Fail_Unauthorized() throws Exception {
		// Given
		SendMessageRequest request = SendMessageRequest.builder()
				.channelId("channel-456")
				.messageType(MessageType.TEXT)
				.payload(Map.of("text", "안녕하세요!"))
				.build();

		// When & Then: 인증 없이 요청
		mockMvc.perform(post("/api/messages")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isUnauthorized());
	}
}
