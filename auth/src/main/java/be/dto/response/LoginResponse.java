package be.dto.response;

public record LoginResponse(
	String accessToken,
	boolean firstLogin
) {}

