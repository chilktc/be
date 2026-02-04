package be.auth.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LoginRequest(
	@NotBlank
	String loginId,
	@NotBlank
	String password
) {
}
