package be.greenroom.ai.dto.request;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PodcastEpisodeRequest(
	UUID user_id,
	String session_id,
	String topic,
	String description,
	Object preferences,
	Object tracing
) {
}
