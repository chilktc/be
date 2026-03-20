package be.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateNicknameRequest(
	@NotBlank
	@Size(min = 2, max = 20)
	String nickname
) {}