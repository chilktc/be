package be.greenroom.tracking.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import be.common.api.ApiResult;
import be.common.api.ErrorCode;
import be.common.docs.ApiErrorCodeExamples;
import be.greenroom.tracking.dto.request.CreateTrackingRequest;
import be.greenroom.tracking.dto.response.TrackingHistoryItemResponse;
import be.greenroom.tracking.service.TrackingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

@Tag(name = "티켓 트래킹", description = "티켓 상태 추적 API")
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/greenroom/tickets")
public class TrackingController {

	private final TrackingService trackingService;

	@Operation(summary = "트래킹 등록", description = "티켓 상태 추적 정보를 등록합니다.")
	@ApiErrorCodeExamples({
		ErrorCode.DOES_NOT_EXIST_TICKET,
		ErrorCode.NO_TICKET_ACCESS,
		ErrorCode.ALREADY_RESOLVED_TICKET,
		ErrorCode.TRACKING_RESOLVED_FIELDS_REQUIRED,
		ErrorCode.TRACKING_UNRESOLVED_FIELDS_REQUIRED,
		ErrorCode.TRACKING_RESOLVED_FIELDS_FORBIDDEN,
		ErrorCode.TRACKING_UNRESOLVED_FIELDS_FORBIDDEN,
		ErrorCode.TRACKING_RESOLVED_ETC_REQUIRED,
		ErrorCode.TRACKING_UNRESOLVED_ETC_REQUIRED,
		ErrorCode.TRACKING_RESOLVED_ETC_FORBIDDEN,
		ErrorCode.TRACKING_UNRESOLVED_ETC_FORBIDDEN,
		ErrorCode.NOTIFICATION_EVENT_SERIALIZATION_FAILED
	})
	@PostMapping("/{ticketId}/tracking")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResult<Void> createTracking(
		@RequestHeader("X-User-Id") @NotBlank String userIdHeader,
		@PathVariable UUID ticketId,
		@RequestBody @Valid CreateTrackingRequest request
	) {
		trackingService.create(UUID.fromString(userIdHeader), ticketId, request);
		return ApiResult.ok();
	}

	@Operation(summary = "트래킹 조회", description = "최신순으로 상태 추적 정보를 조회합니다.")
	@ApiErrorCodeExamples({
		ErrorCode.DOES_NOT_EXIST_TICKET,
		ErrorCode.NO_TICKET_ACCESS,
	})
	@GetMapping("/{ticketId}/tracking")
	@ResponseStatus(HttpStatus.OK)
	public ApiResult<List<TrackingHistoryItemResponse>> getTrackingHistory(
		@RequestHeader("X-User-Id") @NotBlank String userIdHeader,
		@PathVariable UUID ticketId
	) {
		return ApiResult.ok(trackingService.getHistory(UUID.fromString(userIdHeader), ticketId));
	}
}
