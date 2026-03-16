package be.auth.controller;

import java.time.Duration;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import be.auth.dto.LoginResult;
import be.auth.dto.request.SignUpRequest;
import be.auth.dto.request.LoginRequest;
import be.auth.dto.response.LoginResponse;
import be.auth.dto.response.MeResponse;
import be.auth.service.AuthService;
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

@Tag(name = "기본 로그인 인증", description = "인증 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
	private final AuthService authService;

	@Operation(
		summary = "로그인",
		description = "아이디와 비밀번호로 로그인합니다."
	)
	@ApiErrorCodeExamples({
		ErrorCode.FAIL_LOGIN,
		ErrorCode.ACCOUNT_INACTIVATED,
	})
	@PostMapping("/login")
	@ResponseStatus(HttpStatus.OK)
	public ApiResult<LoginResponse> login(
		@RequestBody @Valid LoginRequest request,
		HttpServletResponse response
	) {
		var result = authService.login(request.email(), request.password());

		// Refresh Token을 HttpOnly Cookie로 설정
		response.addHeader(
			"Set-Cookie",
			ResponseCookie.from("refreshToken", result.refreshToken())
				.httpOnly(true)
				// TODO : 배포 서비스에서는 true를 사용
				.secure(true)          // HTTPS 환경에서만
				.sameSite("Lax")
				.path("/auth/refresh")
				.maxAge(Duration.ofDays(14))
				.build()
				.toString()
		);

		return ApiResult.ok(new LoginResponse(result.accessToken(), result.firstLogin()));
	}


	@Operation(
		summary = "토큰 재발급",
		description = "Refresh Token을 이용해 Access Token을 재발급합니다."
	)
	@ApiErrorCodeExamples({
		ErrorCode.INVALID_REFRESH_TOKEN,
		ErrorCode.NOT_FOUND_USER
	})
	@PostMapping("/refresh")
	@ResponseStatus(HttpStatus.OK)
	public ApiResult<LoginResponse> refresh(
		@CookieValue("refreshToken") String refreshToken,
		HttpServletResponse response
	) {
		LoginResult result = authService.refresh(refreshToken);

		response.addHeader(
			"Set-Cookie",
			ResponseCookie.from("refreshToken", result.refreshToken())
				.httpOnly(true)
				.secure(false)
				.sameSite("Lax")
				.path("/auth/refresh")
				.maxAge(Duration.ofDays(14))
				.build()
				.toString()
		);

		return ApiResult.ok(new LoginResponse(result.accessToken(), false));
	}

	@Operation(
		summary = "회원가입",
		description = "회원 가입 API입니다."
	)
	@ApiErrorCodeExamples({
		ErrorCode.EXIST_USER
	})
	@PostMapping("/sign-up")
	@ResponseStatus(HttpStatus.OK)
	public ApiResult<Void> create(@RequestBody @Valid SignUpRequest request) {
		authService.signUp(request.email(), request.password());
		return ApiResult.ok();
	}

	@Operation(
		summary = "로그아웃",
		description = "리프레쉬 토큰을 만료시키고 로그아웃합니다."
	)
	@PostMapping("/logout")
	@ResponseStatus(HttpStatus.OK)
	public ApiResult<Void> logout(
		@RequestHeader(value = "Authorization", required = false) String authorization,
		@CookieValue(value = "refreshToken", required = false) String refreshToken,
		HttpServletResponse response
	) {
		// Bearer 제거
		String accessToken = null;
		if (authorization != null && authorization.startsWith("Bearer ")) {
			accessToken = authorization.substring(7);
		}
		authService.logout(accessToken, refreshToken);

		// refreshToken 쿠키 만료
		response.addHeader(
			"Set-Cookie",
			ResponseCookie.from("refreshToken", "")
				.httpOnly(true)
				.secure(false)
				.sameSite("Lax")
				.path("/auth/refresh")
				.maxAge(0)
				.build()
				.toString()
		);

		return ApiResult.ok();
	}

	@Operation(
		summary = "현재 로그인 사용자 조회",
		description = "현재 로그인한 사용자의 정보를 조회합니다."
	)
	@ApiErrorCodeExamples({
		ErrorCode.NOT_FOUND_USER
	})
	@GetMapping("/me")
	public ApiResult<MeResponse> me(
		@Parameter(hidden = true)
		@RequestHeader(value = "X-User-Id", required = false) UUID userId
	) {
		if (userId == null) {
			throw new CustomException(ErrorCode.JWT_INVALID_TOKEN);
		}
		return ApiResult.ok(authService.getMe(userId));
	}
}