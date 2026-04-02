package be.auth.dto.request;

import be.auth.jwt.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateOrganizationUserRequest(
	@NotBlank
	String name,
	@NotBlank
	String email,
	@NotBlank
	String department,
	@NotBlank
	String position,
	@NotNull
	Role role
) {
}
