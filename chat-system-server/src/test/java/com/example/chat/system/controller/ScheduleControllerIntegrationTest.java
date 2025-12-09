package com.example.chat.system.controller;

import com.example.chat.domain.message.MessageType;
import com.example.chat.system.dto.request.CreateOneTimeScheduleRequest;
import com.example.chat.system.dto.request.CreateRecurringScheduleRequest;
import com.example.chat.system.test.AbstractIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ScheduleController 통합 테스트
 *
 * TestContainers 기반 실제 환경 테스트
 */
@AutoConfigureMockMvc
@DisplayName("ScheduleController 통합 테스트")
class ScheduleControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    @DisplayName("단발성 스케줄 생성 성공")
    void createOneTimeSchedule_Success() throws Exception {
        // Given
        CreateOneTimeScheduleRequest request = CreateOneTimeScheduleRequest.builder()
                .roomId("room-123")
                .channelId("channel-456")
                .messageType(MessageType.TEXT)
                .payload(Map.of("text", "예약 메시지입니다"))
                .executeAt(LocalDateTime.now().plusHours(1))
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/schedules/one-time")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.scheduleId").exists())
                .andExpect(jsonPath("$.data.roomId").value("room-123"))
                .andExpect(jsonPath("$.data.type").value("ONE_TIME"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    @DisplayName("주기적 스케줄 생성 성공")
    void createRecurringSchedule_Success() throws Exception {
        // Given: 매일 오전 9시 실행
        CreateRecurringScheduleRequest request = CreateRecurringScheduleRequest.builder()
                .roomId("room-123")
                .channelId("channel-456")
                .messageType(MessageType.TEXT)
                .payload(Map.of("text", "매일 아침 메시지"))
                .cronExpression("0 0 9 * * ?")
                .maxExecutionCount(30)
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/schedules/recurring")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.type").value("RECURRING"))
                .andExpect(jsonPath("$.data.cronExpression").value("0 0 9 * * ?"))
                .andExpect(jsonPath("$.data.maxExecutionCount").value(30));
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    @DisplayName("스케줄 일시중지 성공")
    void pauseSchedule_Success() throws Exception {
        // Given: 먼저 스케줄 생성
        CreateOneTimeScheduleRequest createRequest = CreateOneTimeScheduleRequest.builder()
                .roomId("room-123")
                .channelId("channel-456")
                .messageType(MessageType.TEXT)
                .payload(Map.of("text", "테스트"))
                .executeAt(LocalDateTime.now().plusHours(1))
                .build();

        String createResponse = mockMvc.perform(post("/api/v1/schedules/one-time")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long scheduleId = objectMapper.readTree(createResponse)
                .get("data")
                .get("scheduleId")
                .asLong();

        // When & Then: 일시중지
        mockMvc.perform(put("/api/v1/schedules/{scheduleId}/pause", scheduleId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PAUSED"));
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    @DisplayName("스케줄 재개 성공")
    void resumeSchedule_Success() throws Exception {
        // Given: 스케줄 생성 후 일시중지
        CreateOneTimeScheduleRequest createRequest = CreateOneTimeScheduleRequest.builder()
                .roomId("room-123")
                .channelId("channel-456")
                .messageType(MessageType.TEXT)
                .payload(Map.of("text", "테스트"))
                .executeAt(LocalDateTime.now().plusHours(1))
                .build();

        String createResponse = mockMvc.perform(post("/api/v1/schedules/one-time")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long scheduleId = objectMapper.readTree(createResponse)
                .get("data")
                .get("scheduleId")
                .asLong();

        // 일시중지
        mockMvc.perform(put("/api/v1/schedules/{scheduleId}/pause", scheduleId));

        // When & Then: 재개
        mockMvc.perform(put("/api/v1/schedules/{scheduleId}/resume", scheduleId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    @DisplayName("스케줄 취소 성공")
    void cancelSchedule_Success() throws Exception {
        // Given: 스케줄 생성
        CreateOneTimeScheduleRequest createRequest = CreateOneTimeScheduleRequest.builder()
                .roomId("room-123")
                .channelId("channel-456")
                .messageType(MessageType.TEXT)
                .payload(Map.of("text", "테스트"))
                .executeAt(LocalDateTime.now().plusHours(1))
                .build();

        String createResponse = mockMvc.perform(post("/api/v1/schedules/one-time")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long scheduleId = objectMapper.readTree(createResponse)
                .get("data")
                .get("scheduleId")
                .asLong();

        // When & Then: 취소
        mockMvc.perform(delete("/api/v1/schedules/{scheduleId}", scheduleId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    @DisplayName("내 스케줄 목록 조회 성공")
    void getMySchedules_Success() throws Exception {
        // Given: 스케줄 2개 생성
        for (int i = 0; i < 2; i++) {
            CreateOneTimeScheduleRequest request = CreateOneTimeScheduleRequest.builder()
                    .roomId("room-" + i)
                    .channelId("channel-456")
                    .messageType(MessageType.TEXT)
                    .payload(Map.of("text", "메시지 " + i))
                    .executeAt(LocalDateTime.now().plusHours(i + 1))
                    .build();

            mockMvc.perform(post("/api/v1/schedules/one-time")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));
        }

        // When & Then
        mockMvc.perform(get("/api/v1/schedules/my"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    @DisplayName("채팅방 스케줄 목록 조회 성공")
    void getSchedulesByRoom_Success() throws Exception {
        // Given: 같은 채팅방에 스케줄 3개 생성
        String roomId = "test-room-999";
        for (int i = 0; i < 3; i++) {
            CreateOneTimeScheduleRequest request = CreateOneTimeScheduleRequest.builder()
                    .roomId(roomId)
                    .channelId("channel-456")
                    .messageType(MessageType.TEXT)
                    .payload(Map.of("text", "메시지 " + i))
                    .executeAt(LocalDateTime.now().plusHours(i + 1))
                    .build();

            mockMvc.perform(post("/api/v1/schedules/one-time")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));
        }

        // When & Then
        mockMvc.perform(get("/api/v1/schedules/room/{roomId}", roomId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(3));
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    @DisplayName("단발성 스케줄 생성 실패 - executeAt이 과거 시간 (Validation)")
    void createOneTimeSchedule_Fail_PastExecuteAt() throws Exception {
        // Given: 과거 시간
        CreateOneTimeScheduleRequest request = CreateOneTimeScheduleRequest.builder()
                .roomId("room-123")
                .channelId("channel-456")
                .messageType(MessageType.TEXT)
                .payload(Map.of("text", "과거 메시지"))
                .executeAt(LocalDateTime.now().minusHours(1))
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/schedules/one-time")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    @DisplayName("주기적 스케줄 생성 실패 - 잘못된 Cron 표현식 (Validation)")
    void createRecurringSchedule_Fail_InvalidCronExpression() throws Exception {
        // Given: 잘못된 Cron 표현식
        CreateRecurringScheduleRequest request = CreateRecurringScheduleRequest.builder()
                .roomId("room-123")
                .channelId("channel-456")
                .messageType(MessageType.TEXT)
                .payload(Map.of("text", "메시지"))
                .cronExpression("INVALID CRON")
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/schedules/recurring")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("스케줄 생성 실패 - 인증 없음 (401 Unauthorized)")
    void createSchedule_Fail_Unauthorized() throws Exception {
        // Given
        CreateOneTimeScheduleRequest request = CreateOneTimeScheduleRequest.builder()
                .roomId("room-123")
                .channelId("channel-456")
                .messageType(MessageType.TEXT)
                .payload(Map.of("text", "메시지"))
                .executeAt(LocalDateTime.now().plusHours(1))
                .build();

        // When & Then: 인증 없이 요청
        mockMvc.perform(post("/api/v1/schedules/one-time")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }
}
