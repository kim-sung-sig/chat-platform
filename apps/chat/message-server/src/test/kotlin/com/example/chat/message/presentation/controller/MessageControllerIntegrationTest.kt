package com.example.chat.message.presentation.controller

import com.example.chat.domain.message.MessageType
import com.example.chat.message.application.dto.request.SendMessageRequest
import com.example.chat.message.application.dto.response.MessageResponse
import com.example.chat.message.test.AbstractIntegrationTest
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

/**
 * MessageController 통합 테스트
 * TestContainers를 사용한 실제 환경 테스트 (Docker 환경 필요)
 */
@Disabled("Requires Docker - run manually")
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("MessageController 통합 테스트")
class MessageControllerIntegrationTest : AbstractIntegrationTest() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    @DisplayName("Health Check - 서버 정상 상태 확인")
    fun healthCheck() {
        // When & Then
        mockMvc.perform(get("/api/messages/health"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(content().string("OK"))
    }

    @Test
    @WithMockUser(username = "testUser")
    @DisplayName("메시지 발송 성공 - 텍스트 메시지")
    fun `sendMessage Success TextMessage`() {
        // Given
        val request = SendMessageRequest(
            channelId = "channel-456",
            messageType = MessageType.TEXT,
            payload = mapOf("text" to "안녕하세요!")
        )

        // When & Then
        mockMvc.perform(
            post("/api/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andDo(print())
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.channelId").value("channel-456"))
            .andExpect(jsonPath("$.messageType").value("TEXT"))
            .andExpect(jsonPath("$.status").exists())
            .andExpect(jsonPath("$.sentAt").exists())
    }

    @Test
    @WithMockUser(username = "testUser")
    @DisplayName("메시지 발송 성공 - 이미지 메시지")
    fun `sendMessage Success ImageMessage`() {
        // Given
        val request = SendMessageRequest(
            channelId = "channel-456",
            messageType = MessageType.IMAGE,
            payload = mapOf(
                "imageUrl" to "https://example.com/image.jpg",
                "width" to 1024,
                "height" to 768
            )
        )

        // When & Then
        mockMvc.perform(
            post("/api/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andDo(print())
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.messageType").value("IMAGE"))
    }

    @Test
    @WithMockUser(username = "testUser")
    @DisplayName("답장 메시지 발송 - replyToMessageId 포함")
    fun `sendReplyMessage Success`() {
        // Given: 먼저 원본 메시지 발송
        val originalRequest = SendMessageRequest(
            channelId = "channel-456",
            messageType = MessageType.TEXT,
            payload = mapOf("text" to "원본 메시지")
        )

        val originalResponse = mockMvc.perform(
            post("/api/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(originalRequest))
        )
            .andReturn()
            .response
            .contentAsString

        val originalMessage = objectMapper.readValue(originalResponse, MessageResponse::class.java)

        // Given: 답장 메시지 요청
        val replyRequest = SendMessageRequest(
            channelId = "channel-456",
            messageType = MessageType.TEXT,
            payload = mapOf("text" to "답장입니다")
        )

        // When & Then
        mockMvc.perform(
            post("/api/messages/reply")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(replyRequest))
        )
            .andDo(print())
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.replyToMessageId").value(originalMessage.id))
    }

    @Test
    @WithMockUser(username = "testUser")
    @DisplayName("메시지 발송 실패 - roomId 누락 (Validation)")
    fun `sendMessage Fail MissingRoomId`() {
        // Given: roomId 없는 요청
        val request = SendMessageRequest(
            channelId = "channel-456",
            messageType = MessageType.TEXT,
            payload = mapOf("text" to "안녕하세요!")
        )

        // When & Then
        mockMvc.perform(
            post("/api/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andDo(print())
            .andExpect(status().isBadRequest)
    }

    @Test
    @WithMockUser(username = "testUser")
    @DisplayName("메시지 발송 실패 - messageType 누락 (Validation)")
    fun `sendMessage Fail MissingMessageType`() {
        // Given: messageType 없는 요청
        val request = SendMessageRequest(
            channelId = "channel-456",
            messageType = null,
            payload = mapOf("text" to "안녕하세요!")
        )

        // When & Then
        mockMvc.perform(
            post("/api/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andDo(print())
            .andExpect(status().isBadRequest)
    }

    @Test
    @DisplayName("메시지 발송 실패 - 인증 없음 (401 Unauthorized)")
    fun `sendMessage Fail Unauthorized`() {
        // Given
        val request = SendMessageRequest(
            channelId = "channel-456",
            messageType = MessageType.TEXT,
            payload = mapOf("text" to "안녕하세요!")
        )

        // When & Then: 인증 없이 요청
        mockMvc.perform(
            post("/api/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andDo(print())
            .andExpect(status().isUnauthorized)
    }
}
