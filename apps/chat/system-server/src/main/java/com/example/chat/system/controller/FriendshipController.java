package com.example.chat.system.controller;

import com.example.chat.system.application.dto.request.FriendshipRequest;
import com.example.chat.system.application.dto.request.SetNicknameRequest;
import com.example.chat.system.application.dto.response.FriendshipResponse;
import com.example.chat.system.application.service.FriendshipApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
 * - Application Service 위임
 */
@RestController
@RequestMapping("/api/friendships")
@Tag(name = "Friendship", description = "친구 관리 API")
@RequiredArgsConstructor
@Slf4j
public class FriendshipController {

    private final FriendshipApplicationService friendshipService;

    @PostMapping
    @Operation(summary = "친구 요청")
    public ResponseEntity<FriendshipResponse> requestFriendship(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody FriendshipRequest request) {
        log.info("POST /api/friendships - userId: {}, friendId: {}", userId, request.friendId());

        FriendshipResponse response = friendshipService.requestFriendship(userId, request.friendId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "친구 목록 조회")
    public ResponseEntity<List<FriendshipResponse>> getFriendList(
            @RequestHeader("X-User-Id") String userId) {
        log.info("GET /api/friendships - userId: {}", userId);

        return ResponseEntity.ok(friendshipService.getFriendList(userId));
    }

    @GetMapping("/pending")
    @Operation(summary = "받은 친구 요청 목록")
    public ResponseEntity<List<FriendshipResponse>> getPendingRequests(
            @RequestHeader("X-User-Id") String userId) {
        log.info("GET /api/friendships/pending - userId: {}", userId);

        return ResponseEntity.ok(friendshipService.getPendingRequests(userId));
    }

    @GetMapping("/sent")
    @Operation(summary = "보낸 친구 요청 목록")
    public ResponseEntity<List<FriendshipResponse>> getSentRequests(
            @RequestHeader("X-User-Id") String userId) {
        log.info("GET /api/friendships/sent - userId: {}", userId);

        return ResponseEntity.ok(friendshipService.getSentRequests(userId));
    }

    @GetMapping("/favorites")
    @Operation(summary = "즐겨찾기 친구 목록")
    public ResponseEntity<List<FriendshipResponse>> getFavoriteFriends(
            @RequestHeader("X-User-Id") String userId) {
        log.info("GET /api/friendships/favorites - userId: {}", userId);

        return ResponseEntity.ok(friendshipService.getFavoriteFriends(userId));
    }

    @PutMapping("/{requestId}/accept")
    @Operation(summary = "친구 요청 수락")
    public ResponseEntity<FriendshipResponse> acceptFriendRequest(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String requestId) {
        log.info("PUT /api/friendships/{}/accept - userId: {}", requestId, userId);

        return ResponseEntity.ok(friendshipService.acceptFriendRequest(userId, requestId));
    }

    @DeleteMapping("/{requestId}/reject")
    @Operation(summary = "친구 요청 거절")
    public ResponseEntity<Void> rejectFriendRequest(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String requestId) {
        log.info("DELETE /api/friendships/{}/reject - userId: {}", requestId, userId);

        friendshipService.rejectFriendRequest(userId, requestId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/users/{friendId}")
    @Operation(summary = "친구 삭제")
    public ResponseEntity<Void> deleteFriend(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String friendId) {
        log.info("DELETE /api/friendships/users/{} - userId: {}", friendId, userId);

        friendshipService.deleteFriend(userId, friendId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/users/{friendId}/block")
    @Operation(summary = "친구 차단")
    public ResponseEntity<FriendshipResponse> blockFriend(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String friendId) {
        log.info("POST /api/friendships/users/{}/block - userId: {}", friendId, userId);

        return ResponseEntity.ok(friendshipService.blockFriend(userId, friendId));
    }

    @DeleteMapping("/users/{friendId}/block")
    @Operation(summary = "친구 차단 해제")
    public ResponseEntity<FriendshipResponse> unblockFriend(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String friendId) {
        log.info("DELETE /api/friendships/users/{}/block - userId: {}", friendId, userId);

        return ResponseEntity.ok(friendshipService.unblockFriend(userId, friendId));
    }

    @PutMapping("/users/{friendId}/nickname")
    @Operation(summary = "친구 별칭 설정")
    public ResponseEntity<FriendshipResponse> setFriendNickname(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String friendId,
            @Valid @RequestBody SetNicknameRequest request) {
        log.info("PUT /api/friendships/users/{}/nickname - userId: {}", friendId, userId);

        return ResponseEntity.ok(friendshipService.setFriendNickname(userId, friendId, request.nickname()));
    }

    @PutMapping("/users/{friendId}/favorite")
    @Operation(summary = "즐겨찾기 토글")
    public ResponseEntity<FriendshipResponse> toggleFavorite(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String friendId) {
        log.info("PUT /api/friendships/users/{}/favorite - userId: {}", friendId, userId);

        return ResponseEntity.ok(friendshipService.toggleFavorite(userId, friendId));
    }
}
