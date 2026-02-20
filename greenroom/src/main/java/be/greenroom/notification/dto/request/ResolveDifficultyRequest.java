package be.greenroom.notification.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ResolveDifficultyRequest(
	@NotBlank String resolvedBy
) {
}
