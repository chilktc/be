package be.auth.jwt;

import java.util.Date;
import java.util.UUID;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtService {
	private final JwtProperties jwtProperties;

	public String issue(UUID id, Role role, Date expiration, String tokenType) {

		return Jwts
			.builder()
			.issuer("shopping-api")
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

	public boolean validate(String token) {
		try {
			Jwts
				.parser()
				.verifyWith(jwtProperties.getSecret())
				.build()
				.parseSignedClaims(token);

			return true;
		} catch (io.jsonwebtoken.JwtException | IllegalArgumentException e) {
			return false;
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

	public String parseName(String token) {
		var id = Jwts
			.parser()
			.verifyWith(jwtProperties.getSecret())
			.build()
			.parseSignedClaims(token)
			.getPayload()
			.get("loginId", String.class);

		return id;
	}

	// public Role parseRole(String token) {
	// 	String role = Jwts
	// 		.parser()
	// 		.verifyWith(jwtProperties.getSecret())
	// 		.build()
	// 		.parseSignedClaims(token)
	// 		.getPayload()
	// 		.get("role", String.class);
	//
	// 	return Role.valueOf(role);
	// }

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
