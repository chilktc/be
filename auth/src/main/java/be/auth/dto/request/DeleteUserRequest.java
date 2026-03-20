package be.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record DeleteUserRequest(
	@NotBlank
	@Email
	String email
) {
}
