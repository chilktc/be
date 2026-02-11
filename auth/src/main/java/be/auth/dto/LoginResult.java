package be.auth.dto;

public record LoginResult(
	String accessToken,
	String refreshToken,
	boolean firstLogin
) {}
