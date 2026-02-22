package com.example.chat.system.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.chat.system.application.dto.response.ChannelMetadataResponse;
import com.example.chat.system.application.service.ChannelMetadataApplicationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/channels")
@Tag(name = "Channel Metadata", description = "채팅방 메타데이터 API")
@RequiredArgsConstructor
@Slf4j
public class ChannelMetadataController {

    private final ChannelMetadataApplicationService metadataService;

    @GetMapping("/{channelId}/metadata")
    @Operation(summary = "메타데이터 조회/생성")
    public ResponseEntity<ChannelMetadataResponse> getOrCreateMetadata(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String channelId) {
        log.info("GET /api/channels/{}/metadata - userId: {}", channelId, userId);
        return ResponseEntity.ok(metadataService.getOrCreateMetadata(userId, channelId));
    }

    @PutMapping("/{channelId}/read")
    @Operation(summary = "읽음 처리")
    public ResponseEntity<ChannelMetadataResponse> markAsRead(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String channelId,
            @RequestParam String messageId) {
        log.info("PUT /api/channels/{}/read - userId: {}, messageId: {}", channelId, userId, messageId);
        return ResponseEntity.ok(metadataService.markAsRead(userId, channelId, messageId));
    }

    @PutMapping("/{channelId}/notification")
    @Operation(summary = "알림 설정 토글")
    public ResponseEntity<ChannelMetadataResponse> toggleNotification(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String channelId) {
        log.info("PUT /api/channels/{}/notification - userId: {}", channelId, userId);
        return ResponseEntity.ok(metadataService.toggleNotification(userId, channelId));
    }

    @PutMapping("/{channelId}/favorite")
    @Operation(summary = "즐겨찾기 토글")
    public ResponseEntity<ChannelMetadataResponse> toggleFavorite(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String channelId) {
        log.info("PUT /api/channels/{}/favorite - userId: {}", channelId, userId);
        return ResponseEntity.ok(metadataService.toggleFavorite(userId, channelId));
    }

    @PutMapping("/{channelId}/pin")
    @Operation(summary = "상단 고정 토글")
    public ResponseEntity<ChannelMetadataResponse> togglePinned(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String channelId) {
        log.info("PUT /api/channels/{}/pin - userId: {}", channelId, userId);
        return ResponseEntity.ok(metadataService.togglePinned(userId, channelId));
    }

    @GetMapping("/favorites")
    @Operation(summary = "즐겨찾기 채팅방")
    public ResponseEntity<List<ChannelMetadataResponse>> getFavorites(
            @RequestHeader("X-User-Id") String userId) {
        log.info("GET /api/channels/favorites - userId: {}", userId);
        return ResponseEntity.ok(metadataService.getFavorites(userId));
    }

    @GetMapping("/pinned")
    @Operation(summary = "상단 고정 채팅방")
    public ResponseEntity<List<ChannelMetadataResponse>> getPinned(
            @RequestHeader("X-User-Id") String userId) {
        log.info("GET /api/channels/pinned - userId: {}", userId);
        return ResponseEntity.ok(metadataService.getPinned(userId));
    }

    @GetMapping("/unread")
    @Operation(summary = "읽지 않은 메시지가 있는 채팅방")
    public ResponseEntity<List<ChannelMetadataResponse>> getWithUnread(
            @RequestHeader("X-User-Id") String userId) {
        log.info("GET /api/channels/unread - userId: {}", userId);
        return ResponseEntity.ok(metadataService.getWithUnread(userId));
    }
}
