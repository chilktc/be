package be.greenroom.ai.dto.request;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SessionCreateRequest(
	UUID user_id,
	String mode
) {
}
