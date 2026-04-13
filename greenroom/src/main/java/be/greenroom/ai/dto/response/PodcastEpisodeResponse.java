package be.greenroom.ai.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PodcastEpisodeResponse(
	@JsonProperty("name")
	String name,
	@JsonProperty("status")
	String status
) {
}
