package be.greenroom.ai.dto.request;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AiStorySelectionRequest(
	String sessionId,
	List<String> keywords,
	String title,
	String description
) {
}
