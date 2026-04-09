package be.greenroom.ai.dto.request;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record CreateMindFrequencyRequest(
	@NotBlank String sessionId,
	@NotEmpty List<@NotBlank String> keywords,
	@NotBlank String description
) {
}
