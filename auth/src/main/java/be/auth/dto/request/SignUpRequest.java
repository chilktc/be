package be.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SignUpRequest(
	@NotBlank
	String email,
	@NotBlank
	String password
) {
}

