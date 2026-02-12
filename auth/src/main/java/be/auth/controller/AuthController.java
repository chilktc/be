package be.auth.controller;

import java.time.Duration;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.CookieValue;
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
import be.auth.service.AuthService;
import be.common.api.ApiResult;
import be.common.api.ErrorCode;
import be.common.docs.ApiErrorCodeExamples;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "기본 로그인 인증(백엔드용)", description = "인증 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
	private final AuthService authService;

	@Operation(summary = "로그인", description = "아이디와 비밀번호로 로그인합니다.")
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

		response.addHeader(
			"Set-Cookie",
			ResponseCookie.from("accessToken", result.accessToken())
				.httpOnly(true)
				.secure(false) // 배포 시 true
				.sameSite("Lax")
				.path("/")
				.maxAge(Duration.ofMinutes(5))
				.build()
				.toString()
		);

		// Refresh Token을 HttpOnly Cookie로 설정
		response.addHeader(
			"Set-Cookie",
			ResponseCookie.from("refreshToken", result.refreshToken())
				.httpOnly(true)
				// TODO : 배포 서비스에서는 true를 사용
				.secure(false)          // HTTPS 환경에서만
				.sameSite("Lax")
				.path("/auth/refresh")
				.maxAge(Duration.ofDays(14))
				.build()
				.toString()
		);

		return ApiResult.ok(new LoginResponse(result.firstLogin()));
	}


	@Operation(summary = "토큰 재발급", description = "Refresh Token을 이용해 Access Token을 재발급합니다.")
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
			ResponseCookie.from("accessToken", result.accessToken())
				.httpOnly(true)
				.secure(false)
				.sameSite("Lax")
				.path("/")
				.maxAge(Duration.ofMinutes(5))
				.build()
				.toString()
		);

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

		return ApiResult.ok(new LoginResponse(false));
	}

	@Operation(summary = "회원가입", description = "회원 가입 API입니다.")
	@ApiErrorCodeExamples({
		ErrorCode.EXIST_USER
	})
	@PostMapping("/sign-up")
	@ResponseStatus(HttpStatus.OK)
	public ApiResult<Void> create(@RequestBody @Valid SignUpRequest request) {
		authService.signUp(request.email(), request.password());
		return ApiResult.ok();
	}

	@PostMapping("/logout")
	@ResponseStatus(HttpStatus.OK)
	public ApiResult<Void> logout(
		@RequestHeader("X-User-Id") String userId,
		@RequestHeader("Authorization") String authorization,
		HttpServletResponse response
	) {
		// Bearer 제거
		String accessToken = authorization.startsWith("Bearer ")
			? authorization.substring(7)
			: authorization;

		authService.logout(UUID.fromString(userId), accessToken);

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
}