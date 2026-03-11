package be.auth.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import be.auth.dto.request.ConsentRequest;
import be.auth.service.AuthNotificationPreferenceService;
import be.auth.service.UserConsentService;
import be.common.api.ApiResult;
import be.common.api.ErrorCode;
import be.common.docs.ApiErrorCodeExamples;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

@Tag(name = "User 개인정보 동의", description = "최초 로그인 개인정보 동의 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class UserConsentController {
	private final UserConsentService userConsentService;
	private final AuthNotificationPreferenceService authNotificationPreferenceService;

	@Operation(
		summary = "개인정보 동의 및 최초 로그인 완료",
		description = "초대받은 사용자가 개인정보 동의를 완료하면 firstLogin 상태가 해제됩니다."
	)
	@ApiErrorCodeExamples({
		ErrorCode.ALREADY_CONSENTED,
		ErrorCode.PRIVACY_NOT_AGREED
	})

	@PostMapping("/consent")
	@ResponseStatus(HttpStatus.OK)
	public ApiResult<Void> consent(

		@RequestHeader("X-User-Id") @NotBlank String userIdHeader,
		@RequestBody @Valid ConsentRequest request
	) {

		userConsentService.completeFirstLogin(UUID.fromString(userIdHeader), request);
		authNotificationPreferenceService.initializeAndPublish(UUID.fromString(userIdHeader));

		return ApiResult.ok();
	}
}
