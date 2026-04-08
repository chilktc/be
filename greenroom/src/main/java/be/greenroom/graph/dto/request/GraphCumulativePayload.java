package be.greenroom.graph.dto.request;

import java.util.List;

public record GraphCumulativePayload(
	List<GraphNodeUpsertItem> nodes,
	List<GraphEdgeUpsertItem> links
) {
}
