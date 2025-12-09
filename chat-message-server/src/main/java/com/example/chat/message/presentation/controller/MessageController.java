package com.example.chat.message.presentation.controller;

import com.example.chat.message.application.dto.request.SendMessageRequest;
import com.example.chat.message.application.dto.response.MessageResponse;
import com.example.chat.message.application.service.MessageApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 메시지 컨트롤러
 *
 * 채팅 메시지 발송 API
 */
@Tag(name = "Message", description = "메시지 발송 API")
@Slf4j
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageApplicationService messageApplicationService;

    /**
     * 메시지 발송
     */
    @Operation(
            summary = "메시지 발송",
            description = "채팅방에 메시지를 발송합니다. 텍스트, 이미지, 혼합 메시지 타입을 지원합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "메시지 발송 성공",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping
    public ResponseEntity<MessageResponse> sendMessage(
            @Valid @RequestBody SendMessageRequest request
    ) {
        log.info("POST /api/messages - channelId: {}, type: {}",
            request.getChannelId(), request.getMessageType());

        MessageResponse response = messageApplicationService.sendMessage(request);


        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Health Check
     */
    @Operation(
            summary = "Health Check",
            description = "서버 상태를 확인합니다."
    )
    @ApiResponse(responseCode = "200", description = "서버 정상")
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}
