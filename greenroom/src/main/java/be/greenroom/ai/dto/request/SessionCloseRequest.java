package be.greenroom.ai.dto.request;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SessionCloseRequest(
	@JsonProperty("user_id")
	UUID userId,
	@JsonProperty("session_id")
	String sessionId,
	@JsonProperty("feedback")
	String feedback
) {
}
