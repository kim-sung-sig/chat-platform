package com.example.chat.websocket.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WebRTC 시그널링 메시지 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebRtcSignalingMessage {
    private String type; // join, leave, offer, answer, ice
    private String roomId; // 음성 채널 ID
    private Long senderId; // 보내는 사람의 ID
    private Long targetId; // 받는 사람의 ID (P2P 1:1 메시지 전용: offer, answer, ice)
    private Object data; // SDP 또는 ICE Candidate 데이터
}
