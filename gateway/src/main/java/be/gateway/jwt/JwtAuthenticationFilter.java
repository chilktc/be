package be.gateway.jwt;

import javax.crypto.SecretKey;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory {

	private final AccessTokenBlacklistService blacklistService;
	private final JwtProperties jwtProperties;

	@Override
	public GatewayFilter apply(Object config) {
		return (exchange, chain) -> {
			// Request Header 토큰 가져오기
			String token = exchange.getRequest()
				.getHeaders()
				.getFirst("Authorization");

			// 토큰이 없을 경우 401 Unauthorized로 응답
			if (token == null || !token.startsWith("Bearer ")) {
				exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
				return exchange.getResponse().setComplete();
			}

			// Bearer 토큰 시 처리
			token = token.substring(7);

			// 정상 로직 -> SecretKey로 토큰 검증 및 Payload(userId 담겨있음) 가져오기
			SecretKey secretKey = jwtProperties.getSecret();
			Claims claims;
			try {
				claims = Jwts.parser()
					.verifyWith(secretKey)
					.build()
					.parseSignedClaims(token)
					.getPayload();
			} catch (ExpiredJwtException e) {
				log.warn("JWT expired: {}", e.getMessage());

				exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
				return exchange.getResponse().setComplete();

			} catch (JwtException | IllegalArgumentException e) {
				log.warn("JWT validation failed: {}", e.getMessage());

				exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
				return exchange.getResponse().setComplete();
			}

			String jti = claims.getId();
			if (jti != null && blacklistService.isExist(jti)) {
				exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
				return exchange.getResponse().setComplete();
			}

			String tokenType = claims.get("token_type", String.class);
			// accesstoken만 진행
			if (!"access".equals(tokenType)) {
				exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
				return exchange.getResponse().setComplete();
			}

			String userId = claims.getSubject();
			String role = claims.get("role", String.class);

			Boolean firstLogin = claims.get("first_login", Boolean.class);
			String path = exchange.getRequest().getURI().getPath();

			if (Boolean.TRUE.equals(firstLogin)
				&& !path.startsWith("/auth/me")
				&& !path.startsWith("/auth/consent")
				&& !path.startsWith("/auth/logout")) {

				exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
				return exchange.getResponse().setComplete();
			}

			// Payload를 X-User-Id 헤더에 담아서 Request
			// Request = 다른 마이크로서비스에 요청을 전달할 때 userId 정보를 담아서 보냄
			return chain.filter(
				exchange.mutate()
					.request(
						exchange.getRequest()
							.mutate()
							.header("X-User-Id", userId)
							.header("X-User-Role", role)
							.build()
					)
					.build()
			);
		};
	}

}