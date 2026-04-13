package be.greenroom.ai.dto.request;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SessionCreateRequest(
	@JsonProperty("user_id")
	UUID userId,
	@JsonProperty("mode")
	String mode
) {
}
