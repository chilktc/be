package be.greenroom.notification.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import be.common.api.ApiResult;
import be.common.api.ErrorCode;
import be.common.docs.ApiErrorCodeExample;
import be.greenroom.notification.dto.request.ResolveDifficultyRequest;
import be.greenroom.notification.dto.request.UpdateNotificationPreferenceRequest;
import be.greenroom.notification.service.GreenroomNotificationCommandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

@Tag(name = "이메일 알림", description = "이메일 알림 API")
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/greenroom/tickets")
public class GreenroomNotificationCommandController {

	private final GreenroomNotificationCommandService commandService;

	@Operation(summary = "알림 최초 등록", description = "최초 그린룸 완료 후 이메일 알림 등록을 진행합니다.")
	@ApiErrorCodeExample(ErrorCode.NOTIFICATION_EVENT_SERIALIZATION_FAILED)
	@PostMapping("/{ticketId}/complete")
	@ResponseStatus(HttpStatus.ACCEPTED)
	public ApiResult<Void> completeTicket(
		@RequestHeader("X-User-Id") @NotBlank String userIdHeader,
		@PathVariable UUID ticketId,
		@RequestBody(required = false) @Valid UpdateNotificationPreferenceRequest request
	) {
		commandService.completeTicket(UUID.fromString(userIdHeader), ticketId, request);
		return ApiResult.ok(null);
	}

	@Operation(summary = "알림 시간 수정", description = "알림 시간을 수정합니다.")
	@ApiErrorCodeExample(ErrorCode.NOTIFICATION_EVENT_SERIALIZATION_FAILED)
	@PostMapping("/{ticketId}/notification-preference")
	@ResponseStatus(HttpStatus.OK)
	public ApiResult<Void> updateNotificationPreference(
		@RequestHeader("X-User-Id") @NotBlank String userIdHeader,
		@PathVariable UUID ticketId,
		@RequestBody @Valid UpdateNotificationPreferenceRequest request
	) {
		commandService.updateNotificationPreference(UUID.fromString(userIdHeader), ticketId, request);
		return ApiResult.ok(null);
	}

	@Operation(summary = "문제 해결 & 알림 중단", description = "알림을 더 이상 받지 않도록 완료처리합니다.")
	@ApiErrorCodeExample(ErrorCode.NOTIFICATION_EVENT_SERIALIZATION_FAILED)
	@PostMapping("/{ticketId}/resolve")
	@ResponseStatus(HttpStatus.OK)
	public ApiResult<Void> resolveDifficulty(
		@RequestHeader("X-User-Id") @NotBlank String userIdHeader,
		@PathVariable UUID ticketId,
		@RequestBody @Valid ResolveDifficultyRequest request
	) {
		commandService.resolveDifficulty(UUID.fromString(userIdHeader), ticketId, request);
		return ApiResult.ok(null);
	}
}
