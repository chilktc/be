package be.greenroom.ai.dto.request;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PodcastEpisodeRequest(
	@JsonProperty("user_id")
	UUID userId,
	@JsonProperty("session_id")
	String sessionId,
	@JsonProperty("situation")
	String situation,
	@JsonProperty("thought")
	String thought,
	@JsonProperty("action")
	String action,
	@JsonProperty("colleague_reaction")
	String colleagueReaction,
	@JsonProperty("learning_pattern")
	Object learningPattern
) {
}
