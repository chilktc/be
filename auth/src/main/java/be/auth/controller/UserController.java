package be.auth.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import be.auth.dto.request.DeleteUserRequest;
import be.auth.dto.request.UpdateNicknameRequest;
import be.auth.service.AuthService;
import be.auth.service.UserService;
import be.common.api.ApiResult;
import be.common.api.CustomException;
import be.common.api.ErrorCode;
import be.common.docs.ApiErrorCodeExamples;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "유저 관련 API", description = "마이페이지 API")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
	private final UserService userService;
	private final AuthService authService;

	@Operation(
		summary = "회원 탈퇴",
		description = "이메일을 입력하여 계정을 삭제합니다."
	)
	@ApiErrorCodeExamples({
		ErrorCode.NOT_FOUND_USER,
		ErrorCode.INVALID_EMAIL,
		ErrorCode.USER_DISABLED
	})
	@DeleteMapping("/me")
	@ResponseStatus(HttpStatus.OK)
	public ApiResult<Void> deleteUser(
		@Parameter(hidden = true)
		@RequestHeader(value = "X-User-Id", required = false) UUID userId,
		@RequestHeader(value = "Authorization", required = false) String authorization,
		@CookieValue(value = "refreshToken", required = false) String refreshToken,
		@RequestBody @Valid DeleteUserRequest request,
		HttpServletResponse response
	) {
		if (userId == null) {
			throw new CustomException(ErrorCode.JWT_INVALID_TOKEN);
		}

		userService.deleteUser(userId, request.email());

		String accessToken = null;
		if (authorization != null && authorization.startsWith("Bearer ")) {
			accessToken = authorization.substring(7);
		}

		authService.logout(accessToken, refreshToken);
		response.addHeader(
			"Set-Cookie",
			ResponseCookie.from("refreshToken", "")
				.httpOnly(true)
				.secure(true)
				.sameSite("None")
				.path("/")
				.maxAge(0)
				.build()
				.toString()
		);

		return ApiResult.ok();
	}

	@Operation(
		summary = "닉네임 수정",
		description = "사용자의 닉네임을 수정합니다."
	)
	@ApiErrorCodeExamples({
		ErrorCode.NOT_FOUND_USER,
		ErrorCode.INVALID_NICKNAME_FORMAT,
		ErrorCode.DUPLICATE_NICKNAME,
		ErrorCode.JWT_INVALID_TOKEN
	})
	@PatchMapping("/me")
	public ApiResult<Void> updateNickname(
		@Parameter(hidden = true)
		@RequestHeader(value = "X-User-Id", required = false) UUID userId,
		@Valid @RequestBody UpdateNicknameRequest request
	) {
		if (userId == null) {
			throw new CustomException(ErrorCode.JWT_INVALID_TOKEN);
		}

		userService.updateNickname(userId, request);
		return ApiResult.ok();
	}
}
