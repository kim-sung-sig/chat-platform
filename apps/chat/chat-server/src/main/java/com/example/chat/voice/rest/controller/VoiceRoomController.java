package com.example.chat.voice.rest.controller;

import com.example.chat.auth.core.util.SecurityUtils;
import com.example.chat.voice.application.service.VoiceRoomCommandService;
import com.example.chat.voice.application.service.VoiceRoomQueryService;
import com.example.chat.voice.domain.model.VoiceParticipant;
import com.example.chat.voice.domain.model.VoiceRoom;
import com.example.chat.voice.rest.dto.response.VoiceParticipantResponse;
import com.example.chat.voice.rest.dto.response.VoiceRoomResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/channels/{channelId}/voice")
@RequiredArgsConstructor
public class VoiceRoomController {

    private final VoiceRoomCommandService commandService;
    private final VoiceRoomQueryService queryService;

    @PostMapping("/join")
    public ResponseEntity<VoiceRoomResponse> join(@PathVariable String channelId) {
        String userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
        VoiceRoom room = commandService.join(channelId, userId);
        return ResponseEntity.ok(toRoomResponse(room));
    }

    @PostMapping("/leave")
    public ResponseEntity<Void> leave(@PathVariable String channelId) {
        String userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
        commandService.leave(channelId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/participants")
    public ResponseEntity<List<VoiceParticipantResponse>> participants(@PathVariable String channelId) {
        String userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
        List<VoiceParticipant> participants = queryService.listParticipants(channelId, userId);
        return ResponseEntity.ok(participants.stream().map(this::toParticipantResponse).toList());
    }

    private VoiceRoomResponse toRoomResponse(VoiceRoom room) {
        return new VoiceRoomResponse(
                room.getChannelId(),
                room.getStatus().name(),
                room.getParticipants().stream().map(this::toParticipantResponse).toList()
        );
    }

    private VoiceParticipantResponse toParticipantResponse(VoiceParticipant participant) {
        return new VoiceParticipantResponse(participant.getUserId(), participant.getJoinedAt());
    }
}
