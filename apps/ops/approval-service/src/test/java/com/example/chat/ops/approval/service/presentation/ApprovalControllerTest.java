package com.example.chat.ops.approval.service.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.chat.common.web.advice.GlobalExceptionHandler;
import com.example.chat.ops.approval.service.application.ApprovalService;
import com.example.chat.ops.approval.service.domain.ApprovalRecord;
import com.example.chat.ops.contract.approval.ApprovalDecision;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = ApprovalController.class, properties = {"spring.cloud.config.enabled=false", "spring.config.import=optional:configserver:"})
@Import(GlobalExceptionHandler.class)
class ApprovalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApprovalService approvalService;

    @Test
    @DisplayName("reviewer can approve")
    void reviewerCanApprove() throws Exception {
        when(approvalService.decide(eq("plan-1"), eq(ApprovalDecision.APPROVE), eq("REVIEWER"), any()))
                .thenReturn(new ApprovalRecord("plan-1", ApprovalDecision.APPROVE, "REVIEWER", Instant.now(), null));

        mockMvc.perform(post("/api/v1/plans/plan-1/approve")
                        .header("X-Project-Role", "REVIEWER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"looks good\""))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("viewer cannot approve")
    void viewerCannotApprove() throws Exception {
        mockMvc.perform(post("/api/v1/plans/plan-1/approve")
                        .header("X-Project-Role", "VIEWER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"looks good\""))
                .andExpect(status().isForbidden());
    }
}
