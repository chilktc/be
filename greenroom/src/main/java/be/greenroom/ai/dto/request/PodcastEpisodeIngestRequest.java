package be.greenroom.ai.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PodcastEpisodeIngestRequest(
	@JsonProperty("session_id")
	@NotBlank String sessionId,
	@JsonProperty("title")
	@NotBlank String title,
	@JsonProperty("image_url")
	String imageUrl,
	@JsonProperty("text")
	@NotBlank String text
) {
}
