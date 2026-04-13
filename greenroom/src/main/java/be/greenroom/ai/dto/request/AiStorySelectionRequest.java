package be.greenroom.ai.dto.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AiStorySelectionRequest(
	@JsonProperty("session_id")
	String sessionId,
	@JsonProperty("keywords")
	List<String> keywords,
	@JsonProperty("title")
	String title,
	@JsonProperty("description")
	String description
) {
}
