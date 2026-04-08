package be.greenroom.graph.dto.response;

import java.util.List;

public record GraphCumulativeDataResponse(
	List<GraphNodeResponse> nodes,
	List<GraphEdgeResponse> links
) {
}
