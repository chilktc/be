package be.dto;

public record LoginResult(
	String accessToken,
	String refreshToken,
	boolean firstLogin
) {}
