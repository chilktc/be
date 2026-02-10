package be.auth.response;

public record GoogleUserInfo(
		String sub,
		String email,
		String name,
		String picture
) {}