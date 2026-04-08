package be.greenroom.graph.dto.response;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GraphNodesResponse(
	@JsonProperty("user_id") UUID userId,
	String type,
	GraphCumulativeDataResponse data
) {
}
