package be.greenroom.ai.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record StorySelectionRequest(
	@NotEmpty List<@NotBlank String> keywords,
	@NotBlank String title,
	@NotBlank String description
) {
}
