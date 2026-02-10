package be.auth.request;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest(
	@NotBlank String code
) {
}