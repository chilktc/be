package be.auth.request;

import jakarta.validation.constraints.NotBlank;

public record SignUpRequest(
	@NotBlank
	String loginId,
	@NotBlank
	String password
) {
}

