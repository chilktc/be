package be.auth.jwt;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Date;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import be.common.api.CustomException;
import be.common.api.ErrorCode;
class JwtServiceTest {

	JwtService jwtService;

	@BeforeEach
	void setUp() {
		JwtProperties prop = new JwtProperties(
			"12345678901234567890123456789012",
			60000L,
			120000L
		);
		jwtService = new JwtService(prop);
	}

	@Test
	@DisplayName("만료된 토큰 검증 테스트")
	void 만료된_토큰_검증() {

		String expiredToken = jwtService.issue(
			UUID.randomUUID(),
			Role.USER,
			new Date(System.currentTimeMillis() - 1000),
			TokenType.ACCESS_TOKEN.getType(),
			false
		);

		CustomException e = assertThrows(
			CustomException.class,
			() -> jwtService.validate(expiredToken)
		);

		assertEquals(ErrorCode.JWT_TOKEN_EXPIRED, e.getErrorCode());
	}

	@Test
	@DisplayName("잘못된 서명 토큰 검증 테스트")
	void 잘못된_서명_토큰_검증() {

		String token = jwtService.issue(
			UUID.randomUUID(),
			Role.USER,
			new Date(System.currentTimeMillis() + 60000),
			TokenType.ACCESS_TOKEN.getType(),
			false
		);

		String tamperedToken = token + "aaa";

		CustomException e = assertThrows(
			CustomException.class,
			() -> jwtService.validate(tamperedToken)
		);

		assertEquals(ErrorCode.JWT_INVALID_SIGNATURE, e.getErrorCode());
	}

	@Test
	@DisplayName("형식이 잘못된 토큰 테스트")
	void 잘못된_형식_토큰_검증() {

		String malformedToken = "invalidToken";

		CustomException e = assertThrows(
			CustomException.class,
			() -> jwtService.validate(malformedToken)
		);

		assertEquals(ErrorCode.JWT_INVALID_TOKEN, e.getErrorCode());
	}

	@Test
	@DisplayName("빈 토큰 검증 테스트")
	void 빈_토큰_검증() {

		CustomException e = assertThrows(
			CustomException.class,
			() -> jwtService.validate("")
		);

		assertEquals(ErrorCode.JWT_EMPTY_TOKEN, e.getErrorCode());
	}

}