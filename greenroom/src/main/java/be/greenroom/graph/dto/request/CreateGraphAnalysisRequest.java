package be.greenroom.graph.dto.request;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;

public record CreateGraphAnalysisRequest(
	@JsonProperty("user_id") @NotNull UUID userId,
	@JsonProperty("episode_id") String episodeId,
	@JsonProperty("session_id") String sessionId,
	@JsonProperty("analysis_type") @NotNull String analysisType,
	@NotNull Object payload
) {
}
