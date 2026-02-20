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
import be.greenroom.notification.dto.request.ResolveDifficultyRequest;
import be.greenroom.notification.dto.request.UpdateNotificationPreferenceRequest;
import be.greenroom.notification.service.GreenroomNotificationCommandService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/greenroom/tickets")
public class GreenroomNotificationCommandController {

	private final GreenroomNotificationCommandService commandService;

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
