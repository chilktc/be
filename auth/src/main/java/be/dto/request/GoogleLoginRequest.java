package be.dto.request;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest(
	@NotBlank String code
) {
}