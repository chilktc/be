package be.auth.dto.response;

import java.util.UUID;

import be.auth.jwt.Role;

public record MeResponse(
	UUID id,
	String email,
	String nickname,
	boolean firstLogin,
	Role role
) {
}
