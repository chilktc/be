package be.greenroom.ai.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import jakarta.validation.constraints.NotBlank;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record PodcastEpisodeIngestRequest(
	@NotBlank String sessionId,
	@NotBlank String title,
	String imageUrl,
	@NotBlank String text
) {
}
