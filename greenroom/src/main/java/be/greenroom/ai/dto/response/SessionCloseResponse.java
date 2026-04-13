package be.greenroom.ai.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SessionCloseResponse(
	@JsonProperty("success")
	boolean success,
	@JsonProperty("message")
	String message
) {
}
