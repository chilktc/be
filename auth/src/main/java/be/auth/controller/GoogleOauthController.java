package be.auth.controller;

import java.time.Duration;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import be.auth.request.GoogleLoginRequest;
import be.auth.response.LoginResponse;
import be.auth.service.AuthService;
import be.auth.service.GoogleOauthService;
import be.common.api.ApiResult;
import be.common.api.ErrorCode;
import be.common.docs.ApiErrorCodeExamples;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Google OAuth 인증", description = "소셜 로그인 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/oauth")
public class GoogleOauthController {

	private final GoogleOauthService googleOauthService;
	private final AuthService authService;

	@Operation(
		summary = "소셜 로그인",
		description = "Authorization Code를 이용해 구글 로그인 후 서비스 토큰을 발급합니다."
	)
	@ApiErrorCodeExamples({
		ErrorCode.FAIL_LOGIN,
		ErrorCode.ACCOUNT_INACTIVATED,
	})
	@PostMapping("/login")
	@ResponseStatus(HttpStatus.OK)

	public ApiResult<LoginResponse> login(
		@RequestBody @Valid GoogleLoginRequest request,
		HttpServletResponse response
	) {
		var pair = googleOauthService.login(request.code());

		String accessToken = pair.getFirst();
		String refreshToken = pair.getSecond();

		// Refresh Token을 HttpOnly Cookie로 설정
		response.addHeader(
			"Set-Cookie",
			ResponseCookie.from("refreshToken", refreshToken)
				.httpOnly(true)
				// TODO : 배포 서비스에서는 true를 사용
				.secure(false)          // HTTPS 환경에서만
				.sameSite("Lax")
				.path("/auth/refresh")
				.maxAge(Duration.ofDays(14))
				.build()
				.toString()
		);

		return ApiResult.ok(new LoginResponse(accessToken));
	}

	@Operation(
		summary = "소셜 로그아웃",
		security = @SecurityRequirement(name = "bearerAuth"),
		description = "토큰을 만료시키고 로그아웃합니다."
	)
	@PostMapping("/logout")
	@ResponseStatus(HttpStatus.OK)
	public ApiResult<Void> googleLogout(
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