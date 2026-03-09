package be.greenroom.notification.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import be.common.api.ApiResult;
import be.greenroom.notification.service.UserNotificationPreferenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

@Tag(name = "사용자 알림 설정", description = "사용자 단위 알림 수신 여부 API")
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/greenroom/notifications")
public class UserNotificationPreferenceController {

	private final UserNotificationPreferenceService preferenceService;

	@Operation(summary = "사용자 알림 수신 설정 토글", description = "요청 시 현재 값을 반전합니다. true면 false, false면 true")
	@PutMapping("/preference")
	@ResponseStatus(HttpStatus.OK)
	public ApiResult<Void> updatePreference(
		@RequestHeader("X-User-Id") @NotBlank String userIdHeader
	) {
		preferenceService.toggle(UUID.fromString(userIdHeader));
		return ApiResult.ok();
	}
}
