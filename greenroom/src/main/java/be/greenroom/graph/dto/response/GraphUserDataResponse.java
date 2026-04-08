package be.greenroom.graph.dto.response;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GraphUserDataResponse(
	List<GraphNodeResponse> nodes,
	List<GraphEdgeResponse> links,
	@JsonProperty("category_distribution") Map<String, Long> categoryDistribution
) {
}
