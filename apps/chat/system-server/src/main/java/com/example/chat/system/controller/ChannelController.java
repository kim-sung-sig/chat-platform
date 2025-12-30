package com.example.chat.system.controller;

import com.example.chat.system.application.service.ChannelApplicationService;
import com.example.chat.system.dto.request.CreateDirectChannelRequest;
import com.example.chat.system.dto.request.CreateGroupChannelRequest;
import com.example.chat.system.dto.request.CreatePrivateChannelRequest;
import com.example.chat.system.dto.request.CreatePublicChannelRequest;
import com.example.chat.system.dto.request.UpdateChannelRequest;
import com.example.chat.system.dto.response.ApiResponse;
import com.example.chat.system.dto.response.ChannelDetailResponse;
import com.example.chat.system.dto.response.ChannelResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 채널 관리 REST Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/channels")
@RequiredArgsConstructor
@Tag(name = "Channel", description = "채널 관리 API")
public class ChannelController {

	private final ChannelApplicationService channelApplicationService;

	/**
	 * 일대일 채널 생성
	 */
	@PostMapping("/direct")
	@Operation(summary = "일대일 채널 생성", description = "두 사용자 간의 일대일 채널을 생성합니다")
	public ResponseEntity<ApiResponse<ChannelResponse>> createDirectChannel(
			@Valid @RequestBody CreateDirectChannelRequest request) {

		log.info("POST /api/v1/channels/direct - targetUserId={}", request.getTargetUserId());

		ChannelResponse response = channelApplicationService.createDirectChannel(request);

		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(ApiResponse.success(response));
	}

	/**
	 * 그룹 채널 생성
	 */
	@PostMapping("/group")
	@Operation(summary = "그룹 채널 생성", description = "여러 사용자가 참여하는 그룹 채널을 생성합니다")
	public ResponseEntity<ApiResponse<ChannelResponse>> createGroupChannel(
			@Valid @RequestBody CreateGroupChannelRequest request) {

		log.info("POST /api/v1/channels/group - name={}", request.getName());

		ChannelResponse response = channelApplicationService.createGroupChannel(request);

		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(ApiResponse.success(response));
	}

	/**
	 * 공개 채널 생성
	 */
	@PostMapping("/public")
	@Operation(summary = "공개 채널 생성", description = "누구나 참여할 수 있는 공개 채널을 생성합니다")
	public ResponseEntity<ApiResponse<ChannelResponse>> createPublicChannel(
			@Valid @RequestBody CreatePublicChannelRequest request) {

		log.info("POST /api/v1/channels/public - name={}", request.getName());

		ChannelResponse response = channelApplicationService.createPublicChannel(request);

		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(ApiResponse.success(response));
	}

	/**
	 * 비공개 채널 생성
	 */
	@PostMapping("/private")
	@Operation(summary = "비공개 채널 생성", description = "초대된 사용자만 참여할 수 있는 비공개 채널을 생성합니다")
	public ResponseEntity<ApiResponse<ChannelResponse>> createPrivateChannel(
			@Valid @RequestBody CreatePrivateChannelRequest request) {

		log.info("POST /api/v1/channels/private - name={}", request.getName());

		ChannelResponse response = channelApplicationService.createPrivateChannel(request);

		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(ApiResponse.success(response));
	}

	/**
	 * 채널 상세 조회
	 */
	@GetMapping("/{channelId}")
	@Operation(summary = "채널 조회", description = "채널 ID로 채널 정보를 조회합니다")
	public ResponseEntity<ApiResponse<ChannelDetailResponse>> getChannel(
			@PathVariable String channelId) {

		log.info("GET /api/v1/channels/{} - channelId={}", channelId, channelId);

		ChannelDetailResponse response = channelApplicationService.getChannel(channelId);

		return ResponseEntity.ok(ApiResponse.success(response));
	}

	/**
	 * 내가 속한 채널 목록 조회
	 */
	@GetMapping("/my")
	@Operation(summary = "내 채널 목록 조회", description = "현재 사용자가 속한 모든 채널을 조회합니다")
	public ResponseEntity<ApiResponse<List<ChannelResponse>>> getMyChannels() {
		log.info("GET /api/v1/channels/my");

		List<ChannelResponse> response = channelApplicationService.getMyChannels();

		return ResponseEntity.ok(ApiResponse.success(response));
	}

	/**
	 * 공개 채널 목록 조회
	 */
	@GetMapping("/public-list")
	@Operation(summary = "공개 채널 목록 조회", description = "모든 공개 채널을 조회합니다")
	public ResponseEntity<ApiResponse<List<ChannelResponse>>> getPublicChannels() {
		log.info("GET /api/v1/channels/public-list");

		List<ChannelResponse> response = channelApplicationService.getPublicChannels();

		return ResponseEntity.ok(ApiResponse.success(response));
	}

	/**
	 * 채널 정보 수정
	 */
	@PutMapping("/{channelId}")
	@Operation(summary = "채널 정보 수정", description = "채널명 및 설명을 수정합니다 (소유자만 가능)")
	public ResponseEntity<ApiResponse<ChannelResponse>> updateChannelInfo(
			@PathVariable String channelId,
			@Valid @RequestBody UpdateChannelRequest request) {

		log.info("PUT /api/v1/channels/{} - channelId={}", channelId, channelId);

		ChannelResponse response = channelApplicationService.updateChannelInfo(channelId, request);

		return ResponseEntity.ok(ApiResponse.success(response));
	}

	/**
	 * 채널 비활성화
	 */
	@DeleteMapping("/{channelId}")
	@Operation(summary = "채널 비활성화", description = "채널을 비활성화합니다 (소유자만 가능)")
	public ResponseEntity<ApiResponse<Void>> deactivateChannel(
			@PathVariable String channelId) {

		log.info("DELETE /api/v1/channels/{} - channelId={}", channelId, channelId);

		channelApplicationService.deactivateChannel(channelId);

		return ResponseEntity.ok(ApiResponse.success(null));
	}

	/**
	 * 채널에 멤버 추가
	 */
	@PostMapping("/{channelId}/members")
	@Operation(summary = "채널에 멤버 추가", description = "채널에 새로운 멤버를 추가합니다")
	public ResponseEntity<ApiResponse<Void>> addMemberToChannel(
			@PathVariable String channelId,
			@RequestParam String userId) {

		log.info("POST /api/v1/channels/{}/members - channelId={}, userId={}",
				channelId, channelId, userId);

		channelApplicationService.addMemberToChannel(channelId, userId);

		return ResponseEntity.ok(ApiResponse.success(null));
	}

	/**
	 * 채널에서 멤버 제거
	 */
	@DeleteMapping("/{channelId}/members/{userId}")
	@Operation(summary = "채널에서 멤버 제거", description = "채널에서 멤버를 제거합니다")
	public ResponseEntity<ApiResponse<Void>> removeMemberFromChannel(
			@PathVariable String channelId,
			@PathVariable String userId) {

		log.info("DELETE /api/v1/channels/{}/members/{} - channelId={}, userId={}",
				channelId, userId, channelId, userId);

		channelApplicationService.removeMemberFromChannel(channelId, userId);

		return ResponseEntity.ok(ApiResponse.success(null));
	}
}
