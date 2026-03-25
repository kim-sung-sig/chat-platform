package com.example.chat.friendship.rest.controller;

import com.example.chat.friendship.rest.dto.request.FriendshipRequest;
import com.example.chat.friendship.rest.dto.request.SetNicknameRequest;
import com.example.chat.friendship.rest.dto.response.FriendshipResponse;
import com.example.chat.auth.core.util.SecurityUtils;
import com.example.chat.friendship.application.service.FriendshipCommandService;
import com.example.chat.friendship.application.service.FriendshipQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 친구 관리 컨트롤러
 *
 * 책임:
 * - HTTP 요청 수신 및 응답
 * - Validation 처리 (@Valid)
 * - Command/Query 서비스 위임 (CQRS)
 */
@RestController
@RequestMapping("/api/v1/friendships")
@Tag(name = "Friendship", description = "친구 관리 API")
@Slf4j
public class FriendshipController {

    private final FriendshipCommandService commandService;
    private final FriendshipQueryService queryService;

    public FriendshipController(FriendshipCommandService commandService, FriendshipQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @PostMapping
    @Operation(summary = "친구 요청")
    public ResponseEntity<FriendshipResponse> requestFriendship(
            @Valid @RequestBody FriendshipRequest request) {
        String userId = currentUserId();
        log.info("POST /api/v1/friendships - userId: {}, friendId: {}", userId, request.friendId());
        return ResponseEntity.status(HttpStatus.CREATED).body(commandService.requestFriendship(userId, request.friendId()));
    }

    @GetMapping
    @Operation(summary = "친구 목록 조회")
    public ResponseEntity<List<FriendshipResponse>> getFriendList() {
        String userId = currentUserId();
        log.info("GET /api/v1/friendships - userId: {}", userId);
        return ResponseEntity.ok(queryService.getFriendList(userId));
    }

    @GetMapping("/pending")
    @Operation(summary = "받은 친구 요청 목록")
    public ResponseEntity<List<FriendshipResponse>> getPendingRequests() {
        String userId = currentUserId();
        log.info("GET /api/v1/friendships/pending - userId: {}", userId);
        return ResponseEntity.ok(queryService.getPendingRequests(userId));
    }

    @GetMapping("/sent")
    @Operation(summary = "보낸 친구 요청 목록")
    public ResponseEntity<List<FriendshipResponse>> getSentRequests() {
        String userId = currentUserId();
        log.info("GET /api/v1/friendships/sent - userId: {}", userId);
        return ResponseEntity.ok(queryService.getSentRequests(userId));
    }

    @GetMapping("/favorites")
    @Operation(summary = "즐겨찾기 친구 목록")
    public ResponseEntity<List<FriendshipResponse>> getFavoriteFriends() {
        String userId = currentUserId();
        log.info("GET /api/v1/friendships/favorites - userId: {}", userId);
        return ResponseEntity.ok(queryService.getFavoriteFriends(userId));
    }

    @PutMapping("/{requestId}/accept")
    @Operation(summary = "친구 요청 수락")
    public ResponseEntity<FriendshipResponse> acceptFriendRequest(
            @PathVariable String requestId) {
        String userId = currentUserId();
        log.info("PUT /api/v1/friendships/{}/accept - userId: {}", requestId, userId);
        return ResponseEntity.ok(commandService.acceptFriendRequest(userId, requestId));
    }

    @DeleteMapping("/{requestId}/reject")
    @Operation(summary = "친구 요청 거절")
    public ResponseEntity<Void> rejectFriendRequest(
            @PathVariable String requestId) {
        String userId = currentUserId();
        log.info("DELETE /api/v1/friendships/{}/reject - userId: {}", requestId, userId);
        commandService.rejectFriendRequest(userId, requestId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/users/{friendId}")
    @Operation(summary = "친구 삭제")
    public ResponseEntity<Void> deleteFriend(
            @PathVariable String friendId) {
        String userId = currentUserId();
        log.info("DELETE /api/v1/friendships/users/{} - userId: {}", friendId, userId);
        commandService.deleteFriend(userId, friendId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/users/{friendId}/block")
    @Operation(summary = "친구 차단")
    public ResponseEntity<FriendshipResponse> blockFriend(
            @PathVariable String friendId) {
        String userId = currentUserId();
        log.info("POST /api/v1/friendships/users/{}/block - userId: {}", friendId, userId);
        return ResponseEntity.ok(commandService.blockFriend(userId, friendId));
    }

    @DeleteMapping("/users/{friendId}/block")
    @Operation(summary = "친구 차단 해제")
    public ResponseEntity<FriendshipResponse> unblockFriend(
            @PathVariable String friendId) {
        String userId = currentUserId();
        log.info("DELETE /api/v1/friendships/users/{}/block - userId: {}", friendId, userId);
        return ResponseEntity.ok(commandService.unblockFriend(userId, friendId));
    }

    @PutMapping("/users/{friendId}/nickname")
    @Operation(summary = "친구 별칭 설정")
    public ResponseEntity<FriendshipResponse> setFriendNickname(
            @PathVariable String friendId,
            @Valid @RequestBody SetNicknameRequest request) {
        String userId = currentUserId();
        log.info("PUT /api/v1/friendships/users/{}/nickname - userId: {}", friendId, userId);
        return ResponseEntity.ok(commandService.setFriendNickname(userId, friendId, request.nickname()));
    }

    @PutMapping("/users/{friendId}/favorite")
    @Operation(summary = "즐겨찾기 토글")
    public ResponseEntity<FriendshipResponse> toggleFavorite(
            @PathVariable String friendId) {
        String userId = currentUserId();
        log.info("PUT /api/v1/friendships/users/{}/favorite - userId: {}", friendId, userId);
        return ResponseEntity.ok(commandService.toggleFavorite(userId, friendId));
    }

    private String currentUserId() {
        return SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
    }
}
