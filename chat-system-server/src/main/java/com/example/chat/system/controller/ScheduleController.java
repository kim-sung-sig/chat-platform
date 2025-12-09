package com.example.chat.system.controller;

import com.example.chat.system.dto.request.CreateOneTimeScheduleRequest;
import com.example.chat.system.dto.request.CreateRecurringScheduleRequest;
import com.example.chat.system.dto.response.ApiResponse;
import com.example.chat.system.dto.response.ScheduleResponse;
import com.example.chat.system.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 스케줄 컨트롤러
 *
 * 예약 메시지 스케줄 관리 API
 */
@Tag(name = "Schedule", description = "예약 메시지 스케줄 API")
@Slf4j
@RestController
@RequestMapping("/api/v1/schedules")
@RequiredArgsConstructor
public class ScheduleController {

	private final ScheduleService scheduleService;

	/**
	 * 단발성 스케줄 생성
	 */
	@Operation(
			summary = "단발성 스케줄 생성",
			description = "특정 시간에 1회 실행되는 예약 메시지 스케줄을 생성합니다."
	)
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(
					responseCode = "201",
					description = "스케줄 생성 성공"
			),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(
					responseCode = "400",
					description = "잘못된 요청 (executeAt이 과거 시간)"
			)
	})
	@PostMapping("/one-time")
	public ResponseEntity<ApiResponse<ScheduleResponse>> createOneTimeSchedule(
			@Valid @RequestBody CreateOneTimeScheduleRequest request
	) {
		log.info("POST /api/v1/schedules/one-time - roomId: {}", request.getRoomId());

		ScheduleResponse response = scheduleService.createOneTimeSchedule(request);

		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(ApiResponse.success(response));
	}

	/**
	 * 주기적 스케줄 생성
	 */
	@Operation(
			summary = "주기적 스케줄 생성",
			description = "Cron 표현식 기반으로 반복 실행되는 예약 메시지 스케줄을 생성합니다. 예: '0 0 9 * * ?' = 매일 오전 9시"
	)
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "스케줄 생성 성공"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 Cron 표현식")
	})
	@PostMapping("/recurring")
	public ResponseEntity<ApiResponse<ScheduleResponse>> createRecurringSchedule(
			@Valid @RequestBody CreateRecurringScheduleRequest request
	) {
		log.info("POST /api/v1/schedules/recurring - roomId: {}, cron: {}",
				request.getRoomId(), request.getCronExpression());

		ScheduleResponse response = scheduleService.createRecurringSchedule(request);

		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(ApiResponse.success(response));
	}

	/**
	 * 스케줄 일시중지
	 */
	@Operation(summary = "스케줄 일시중지", description = "활성 상태의 스케줄을 일시중지합니다.")
	@Parameter(name = "scheduleId", description = "스케줄 ID", required = true)
	@PutMapping("/{scheduleId}/pause")
	public ResponseEntity<ApiResponse<ScheduleResponse>> pauseSchedule(
			@PathVariable Long scheduleId
	) {
		log.info("PUT /api/v1/schedules/{}/pause", scheduleId);

		ScheduleResponse response = scheduleService.pauseSchedule(scheduleId);

		return ResponseEntity.ok(ApiResponse.success(response));
	}

	/**
	 * 스케줄 재개
	 */
	@Operation(summary = "스케줄 재개", description = "일시중지된 스케줄을 재개합니다.")
	@Parameter(name = "scheduleId", description = "스케줄 ID", required = true)
	@PutMapping("/{scheduleId}/resume")
	public ResponseEntity<ApiResponse<ScheduleResponse>> resumeSchedule(
			@PathVariable Long scheduleId
	) {
		log.info("PUT /api/v1/schedules/{}/resume", scheduleId);

		ScheduleResponse response = scheduleService.resumeSchedule(scheduleId);

		return ResponseEntity.ok(ApiResponse.success(response));
	}

	/**
	 * 스케줄 취소
	 */
	@Operation(summary = "스케줄 취소", description = "스케줄을 취소하고 Quartz Job을 삭제합니다.")
	@Parameter(name = "scheduleId", description = "스케줄 ID", required = true)
	@DeleteMapping("/{scheduleId}")
	public ResponseEntity<ApiResponse<Void>> cancelSchedule(
			@PathVariable Long scheduleId
	) {
		log.info("DELETE /api/v1/schedules/{}", scheduleId);

		scheduleService.cancelSchedule(scheduleId);

		return ResponseEntity.ok(ApiResponse.success(null));
	}

	/**
	 * 내 스케줄 목록 조회
	 */
	@Operation(summary = "내 스케줄 목록 조회", description = "현재 사용자의 활성 스케줄 목록을 조회합니다.")
	@GetMapping("/my")
	public ResponseEntity<ApiResponse<List<ScheduleResponse>>> getMySchedules() {
		log.info("GET /api/v1/schedules/my");

		List<ScheduleResponse> response = scheduleService.getMySchedules();

		return ResponseEntity.ok(ApiResponse.success(response));
	}

	/**
	 * 채팅방의 스케줄 목록 조회
	 */
	@Operation(summary = "채팅방 스케줄 목록 조회", description = "특정 채팅방의 활성 스케줄 목록을 조회합니다.")
	@Parameter(name = "roomId", description = "채팅방 ID", required = true)
	@GetMapping("/room/{roomId}")
	public ResponseEntity<ApiResponse<List<ScheduleResponse>>> getSchedulesByRoom(
			@PathVariable String roomId
	) {
		log.info("GET /api/v1/schedules/room/{}", roomId);

		List<ScheduleResponse> response = scheduleService.getSchedulesByRoom(roomId);

		return ResponseEntity.ok(ApiResponse.success(response));
	}
}
