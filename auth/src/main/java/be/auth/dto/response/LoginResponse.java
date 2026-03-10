package be.auth.dto.response;

public record LoginResponse(
	String accessToken,
	boolean firstLogin
) {}

