package be.greenroom.ai.dto.request;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record PodcastEpisodeRequest(
	UUID userId,
	String sessionId,
	String situation,
	String thought,
	String action,
	String colleagueReaction,
	Object learningPattern
) {
}
