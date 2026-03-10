package be.auth.jwt;

import java.util.Date;
import java.util.UUID;

import org.springframework.stereotype.Component;

import be.common.api.CustomException;
import be.common.api.ErrorCode;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtService {
	private final JwtProperties jwtProperties;

	public String issue(UUID id, Role role, Date expiration, String tokenType) {

		return Jwts
			.builder()
			.issuer("auth-service")
			.subject(id.toString())
			.claim("role", role.name())
			.claim("token_type", tokenType)
			.id(UUID.randomUUID().toString())
			.issuedAt(new Date())
			.expiration(expiration)
			.signWith(jwtProperties.getSecret())
			.compact();
	}

	public Date getAccessExpiration() {
		return jwtProperties.getAccessTokenExpiration();
	}

	public Date getRefreshExpiration() {
		return jwtProperties.getRefreshTokenExpiration();
	}

	public void validate(String token) {
		try {
			Jwts
				.parser()
				.verifyWith(jwtProperties.getSecret())
				.build()
				.parseSignedClaims(token);

		} catch (io.jsonwebtoken.ExpiredJwtException e) {
			throw new CustomException(ErrorCode.JWT_TOKEN_EXPIRED);

		} catch (io.jsonwebtoken.SignatureException e) {
			throw new CustomException(ErrorCode.JWT_INVALID_SIGNATURE);

		} catch (io.jsonwebtoken.UnsupportedJwtException e) {
			throw new CustomException(ErrorCode.JWT_UNSUPPORTED_TOKEN);

		} catch (io.jsonwebtoken.MalformedJwtException e) {
			throw new CustomException(ErrorCode.JWT_INVALID_TOKEN);

		} catch (IllegalArgumentException e) {
			throw new CustomException(ErrorCode.JWT_EMPTY_TOKEN);
		}
	}

	public UUID parseId(String token) {
		var id = Jwts
			.parser()
			.verifyWith(jwtProperties.getSecret())
			.build()
			.parseSignedClaims(token)
			.getPayload()
			.getSubject();

		return UUID.fromString(id);
	}


	public String parseJti(String token) {
		return Jwts
			.parser()
			.verifyWith(jwtProperties.getSecret())
			.build()
			.parseSignedClaims(token)
			.getPayload()
			.getId();
	}

	public Date parseExpiration(String token) {
		return Jwts
			.parser()
			.verifyWith(jwtProperties.getSecret())
			.build()
			.parseSignedClaims(token)
			.getPayload()
			.getExpiration();
	}
}
