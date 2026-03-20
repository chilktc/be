package be.auth.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import be.auth.dto.request.DeleteUserRequest;
import be.auth.service.UserService;
import be.common.api.ApiResult;
import be.common.api.CustomException;
import be.common.api.ErrorCode;
import be.common.docs.ApiErrorCodeExamples;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "회원 탈퇴", description = "회원 soft delete API")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class UserController {
	private final UserService userService;

	@Operation(
		summary = "회원 탈퇴",
		description = "이메일을 입력하여 계정을 삭제합니다."
	)
	@ApiErrorCodeExamples({
		ErrorCode.NOT_FOUND_USER,
		ErrorCode.INVALID_EMAIL,
		ErrorCode.ALREADY_DELETED_USER
	})
	@PostMapping("/withdraw")
	@ResponseStatus(HttpStatus.OK)
	public ApiResult<Void> deleteUser(
		@Parameter(hidden = true)
		@RequestHeader(value = "X-User-Id", required = false) UUID userId,
		@RequestBody @Valid DeleteUserRequest request
	) {
		if (userId == null) {
			throw new CustomException(ErrorCode.JWT_INVALID_TOKEN);
		}

		if (request == null || request.email() == null || request.email().isBlank()) {
			throw new CustomException(ErrorCode.INVALID_EMAIL);
		}

		userService.deleteUser(userId, request.email());
		return ApiResult.ok();
	}
}
