package be.auth.service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import be.auth.domain.OauthProvider;
import be.auth.domain.User;
import be.auth.jwt.JwtService;
import be.auth.jwt.TokenType;
import be.auth.repository.UserRepository;
import be.common.utils.Preconditions;
import be.auth.dto.LoginResult;
import be.auth.dto.response.GoogleTokenResponse;
import be.auth.dto.response.GoogleUserInfo;
import be.common.api.CustomException;
import be.common.api.ErrorCode;

import static org.springframework.web.client.HttpClientErrorException.BadRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class GoogleOauthService {

	private final RestTemplate restTemplate;
	private final JwtService jwtService;
	private final UserRepository userRepository;
	private final RefreshTokenService refreshTokenService;

	@Value("${oauth.google.client-id}")
	private String clientId;

	@Value("${oauth.google.client-secret}")
	private String clientSecret;

	@Value("${oauth.google.redirect-uri}")
	private String redirectUri;

	private static final String GOOGLE_TOKEN_URL =
		"https://oauth2.googleapis.com/token";

	private static final String GOOGLE_USERINFO_URL =
		"https://openidconnect.googleapis.com/v1/userinfo";

	private String normalizeAuthCode(String code) {
		String once = URLDecoder.decode(code, StandardCharsets.UTF_8);
		return URLDecoder.decode(once, StandardCharsets.UTF_8);
	}

	public LoginResult login(String code) {
		String normalized = normalizeAuthCode(code);

		GoogleTokenResponse token = exchangeCode(normalized);
		GoogleUserInfo userInfo = fetchUserInfo(token.accessToken());

		String email = userInfo.email();
		String googleSub = userInfo.sub();

		User user = userRepository.findByEmail(email)
			.orElseThrow(() -> new CustomException(ErrorCode.OAUTH_EMAIL_NOT_REGISTERED));

		if (user.getProvider() == null) {
			user.bindGoogleOAuth(googleSub);
		}
		else if (user.getProvider() == OauthProvider.GOOGLE) {
			Preconditions.validate(
				user.getProviderUserId().equals(googleSub),
				ErrorCode.OAUTH_PROVIDER_MISMATCH
			);
		}

		var accessExp = jwtService.getAccessExpiration();
		var refreshExp = jwtService.getRefreshExpiration();

		var accessToken = jwtService.issue(user.getId(), user.getRole(), accessExp, TokenType.ACCESS_TOKEN.getType());
		var refreshToken = jwtService.issue(user.getId(), user.getRole(), refreshExp, TokenType.REFRESH_TOKEN.getType());

		long refreshTtlMs = refreshExp.getTime() - System.currentTimeMillis();
		refreshTokenService.save(user.getId(), refreshToken, refreshTtlMs);

		return new LoginResult(accessToken, refreshToken, user.isFirstLogin());
}

	private GoogleTokenResponse exchangeCode(String code) {
		try {
			MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
			body.add("code", code);
			body.add("client_id", clientId);
			body.add("client_secret", clientSecret);
			body.add("redirect_uri", redirectUri);
			body.add("grant_type", "authorization_code");

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

			return restTemplate.postForObject(
				GOOGLE_TOKEN_URL,
				new HttpEntity<>(body, headers),
				GoogleTokenResponse.class
			);

		} catch (BadRequest e) {
			throw new CustomException(ErrorCode.OAUTH_INVALID_CODE);

		} catch (RestClientException e) {
			throw new CustomException(ErrorCode.OAUTH_TOKEN_EXCHANGE_FAILED);
		}
	}

	private GoogleUserInfo fetchUserInfo(String accessToken) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setBearerAuth(accessToken);

			var response = restTemplate.exchange(
				GOOGLE_USERINFO_URL,
				HttpMethod.GET,
				new HttpEntity<>(headers),
				GoogleUserInfo.class
			);

			if (response.getBody() == null) {
				throw new CustomException(ErrorCode.OAUTH_USERINFO_FAILED);
			}

			return response.getBody();

		} catch (RestClientException e) {
			throw new CustomException(ErrorCode.OAUTH_USERINFO_FAILED);
		}
	}
}