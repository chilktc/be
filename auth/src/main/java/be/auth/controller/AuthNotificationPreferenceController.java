package be.auth.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import be.auth.dto.response.NotificationPreferenceResponse;
import be.auth.service.AuthNotificationPreferenceService;
import be.common.api.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

@Tag(name = "알림 설정", description = "사용자 알림 수신 설정 API")
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/auth/notifications")
public class AuthNotificationPreferenceController {

	private final AuthNotificationPreferenceService preferenceService;

	@Operation(summary = "알림 수신 설정 조회")
	@GetMapping("/preference")
	public ApiResult<NotificationPreferenceResponse> get(
		@RequestHeader("X-User-Id") @NotBlank String userIdHeader
	) {
		return ApiResult.ok(preferenceService.get(UUID.fromString(userIdHeader)));
	}

	@Operation(summary = "알림 수신 설정 토글")
	@PutMapping("/preference")
	@ResponseStatus(HttpStatus.OK)
	public ApiResult<NotificationPreferenceResponse> toggle(
		@RequestHeader("X-User-Id") @NotBlank String userIdHeader
	) {
		return ApiResult.ok(preferenceService.toggle(UUID.fromString(userIdHeader)));
	}
}
