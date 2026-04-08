package be.greenroom.graph.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import be.greenroom.graph.domain.UserGraphEdge;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GraphEdgeResponse(
	@JsonProperty("source_label") String sourceLabel,
	@JsonProperty("source_grp") String sourceGrp,
	@JsonProperty("target_label") String targetLabel,
	@JsonProperty("target_grp") String targetGrp,
	Integer weight,
	String relationship,
	@JsonProperty("first_seen") LocalDateTime firstSeen,
	@JsonProperty("last_seen") LocalDateTime lastSeen
) {
	public static GraphEdgeResponse forCumulative(UserGraphEdge edge) {
		return new GraphEdgeResponse(
			edge.getSourceLabel(),
			edge.getSourceGroup().toApiValue(),
			edge.getTargetLabel(),
			edge.getTargetGroup().toApiValue(),
			edge.getWeight(),
			edge.getRelationship(),
			edge.getFirstSeen(),
			edge.getLastSeen()
		);
	}

	public static GraphEdgeResponse forVisualization(UserGraphEdge edge) {
		return new GraphEdgeResponse(
			edge.getSourceLabel(),
			null,
			edge.getTargetLabel(),
			null,
			edge.getWeight(),
			null,
			null,
			null
		);
	}
}
