package be.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import be.auth.dto.request.InviteUserRequest;
import be.auth.service.AdminInviteService;
import be.common.api.ApiResult;
import be.common.api.CustomException;
import be.common.api.ErrorCode;
import be.common.docs.ApiErrorCodeExamples;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Admin의 조직원 초대", description = "조직원 초대 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/users")
public class AdminInviteController {
	private final AdminInviteService adminInviteService;

	@Operation(
		summary = "조직원 초대",
		description = "관리자가 조직원 이메일을 등록하면 초대 메일을 발송합니다."
	)
	@ApiErrorCodeExamples({
		ErrorCode.EXIST_USER,
		ErrorCode.ACCESS_DENIED
	})

	@PostMapping("/invite")
	@ResponseStatus(HttpStatus.OK)
	public ApiResult<Void> inviteUser(
		@Parameter(hidden = true)
		@RequestHeader("X-User-Role") String role,
		@RequestBody @Valid InviteUserRequest request
	) {
		if (!"ADMIN".equals(role)) {
			throw new CustomException(ErrorCode.ACCESS_DENIED);
		}
		adminInviteService.inviteUser(request);
		return ApiResult.ok();
	}
}