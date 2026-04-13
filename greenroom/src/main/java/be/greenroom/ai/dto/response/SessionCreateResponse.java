package be.greenroom.ai.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SessionCreateResponse(
	@JsonProperty("session_id")
	String sessionId,
	@JsonProperty("mode")
	String mode,
	@JsonProperty("created_at")
	String createdAt
) {
}
