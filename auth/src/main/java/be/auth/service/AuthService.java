package be.auth.service;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import be.auth.domain.OauthProvider;
import be.auth.domain.User;
import be.auth.jwt.JwtService;
import be.auth.jwt.Role;
import be.auth.jwt.TokenType;
import be.auth.repository.UserRepository;
import be.common.api.CustomException;
import be.common.api.ErrorCode;
import be.common.utils.Preconditions;
import be.auth.dto.LoginResult;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final RefreshTokenService refreshTokenService;
	private final AccessTokenBlacklistService accessTokenBlacklistService;

	public LoginResult login(String email, String password) {
		var user = userRepository.findByEmail(email)
			.orElseThrow(() -> new CustomException(ErrorCode.FAIL_LOGIN));

		Preconditions.validate(user.getProvider() == OauthProvider.SERVER, ErrorCode.FAIL_LOGIN);
		Preconditions.validate(passwordEncoder.matches(password, user.getPassword()), ErrorCode.FAIL_LOGIN);
		Preconditions.validate(user.isActive(), ErrorCode.ACCOUNT_INACTIVATED);

		var accessExp = jwtService.getAccessExpiration();
		var refreshExp = jwtService.getRefreshExpiration();

		var accessToken = jwtService.issue(user.getId(), user.getRole(), accessExp, TokenType.ACCESS_TOKEN.getType());
		var refreshToken = jwtService.issue(user.getId(), user.getRole(), refreshExp, TokenType.REFRESH_TOKEN.getType());

		long refreshTtlMs = refreshExp.getTime() - System.currentTimeMillis();
		refreshTokenService.save(user.getId(), refreshToken, refreshTtlMs);

		return new LoginResult(accessToken, refreshToken, user.isFirstLogin());
	}

	public LoginResult refresh(String refreshToken) {
		UUID userId;
		try {
			userId = jwtService.parseId(refreshToken);
		} catch (JwtException | IllegalArgumentException e) {
			throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
		}

		if (!refreshTokenService.isSame(userId, refreshToken)) {
			throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
		}

		var user = userRepository.findByIdOrThrow(userId, ErrorCode.NOT_FOUND_USER);

		Preconditions.validate(user.isActive(), ErrorCode.ACCOUNT_INACTIVATED);

		var newAccessExp = jwtService.getAccessExpiration();
		var newRefreshExp = jwtService.getRefreshExpiration();

		var newAccessToken = jwtService.issue(user.getId(), user.getRole(), newAccessExp, TokenType.ACCESS_TOKEN.getType());
		var newRefreshToken = jwtService.issue(user.getId(), user.getRole(), newRefreshExp, TokenType.REFRESH_TOKEN.getType());

		long newRefreshTtlMs = newRefreshExp.getTime() - System.currentTimeMillis();
		refreshTokenService.save(user.getId(), newRefreshToken, newRefreshTtlMs);

		return new LoginResult(newAccessToken, newRefreshToken, false);
	}

	public void logout(String accessToken, String refreshToken) {

		try {
			if (refreshToken != null) {
				UUID userId = jwtService.parseId(refreshToken);
				refreshTokenService.delete(userId);
			}
		} catch (JwtException | IllegalArgumentException e) {
		}

		try {
			if (accessToken != null) {
				String jti = jwtService.parseJti(accessToken);
				var exp = jwtService.parseExpiration(accessToken);

				long ttlMs = exp.getTime() - System.currentTimeMillis();
				if (ttlMs > 0) {
					accessTokenBlacklistService.save(jti, ttlMs);
				}
			}
		} catch (JwtException | IllegalArgumentException e) {
		}
	}

	public void signUp(String email, String password) {
		Preconditions.validate(
			!userRepository.existsByEmail(email),
			ErrorCode.EXIST_USER
		);

		String encodedPassword = passwordEncoder.encode(password);

		User user = User.createServerUser(
			UUID.randomUUID(),
			email,
			encodedPassword,
			Role.USER
		);

		userRepository.save(user);
	}

}
