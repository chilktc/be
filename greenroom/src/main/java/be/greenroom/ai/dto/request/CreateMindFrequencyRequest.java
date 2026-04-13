package be.greenroom.ai.dto.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record CreateMindFrequencyRequest(
	@JsonProperty("session_id")
	@NotBlank String sessionId,
	@JsonProperty("keywords")
	@NotEmpty List<@NotBlank String> keywords,
	@JsonProperty("description")
	@NotBlank String description
) {
}
