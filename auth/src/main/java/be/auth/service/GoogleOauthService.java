package be.auth.service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import be.auth.domain.OauthProvider;
import be.auth.domain.User;
import be.auth.jwt.JwtService;
import be.auth.jwt.Role;
import be.auth.jwt.TokenType;
import be.auth.repository.UserRepository;
import be.auth.response.GoogleTokenResponse;
import be.auth.response.GoogleUserInfo;
import be.common.api.CustomException;
import be.common.api.ErrorCode;

import static org.springframework.web.client.HttpClientErrorException.BadRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
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

	private String normalizeAuthCode(String code) {
		String once = URLDecoder.decode(code, StandardCharsets.UTF_8);
		return URLDecoder.decode(once, StandardCharsets.UTF_8);
	}

	public Pair<String, String> login(String code) {
		String normalized = normalizeAuthCode(code);

		GoogleTokenResponse token = exchangeCode(normalized);
		GoogleUserInfo userInfo = fetchUserInfo(token.accessToken());

		User user = userRepository.findByProviderAndProviderUserId(
			OauthProvider.GOOGLE,
			userInfo.sub()
		).orElseGet(() ->
			userRepository.save(
				User.createGoogleUser(
					UUID.randomUUID(),
					userInfo.sub(),
					Role.USER
				)
			)
		);

		var accessExp = jwtService.getAccessExpiration();
		var refreshExp = jwtService.getRefreshExpiration();

		var accessToken = jwtService.issue(user.getId(), user.getRole(), accessExp, TokenType.ACCESS_TOKEN.getType());
		var refreshToken = jwtService.issue(user.getId(), user.getRole(), refreshExp, TokenType.REFRESH_TOKEN.getType());

		long refreshTtlMs = refreshExp.getTime() - System.currentTimeMillis();
		refreshTokenService.save(user.getId(), refreshToken, refreshTtlMs);

		return Pair.of(accessToken, refreshToken);
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
				"https://oauth2.googleapis.com/token",
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
				"https://openidconnect.googleapis.com/v1/userinfo",
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