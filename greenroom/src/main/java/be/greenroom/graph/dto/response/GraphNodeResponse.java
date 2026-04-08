package be.greenroom.graph.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import be.greenroom.graph.domain.UserGraphNode;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GraphNodeResponse(
	String label,
	String group,
	@JsonProperty("grp") String grp,
	Double weight,
	@JsonProperty("mention_count") Integer mentionCount,
	String trend,
	@JsonProperty("first_seen") LocalDateTime firstSeen,
	@JsonProperty("last_seen") LocalDateTime lastSeen
) {
	public static GraphNodeResponse forCumulative(UserGraphNode node) {
		return new GraphNodeResponse(
			node.getLabel(),
			null,
			node.getGroup().toApiValue(),
			node.getWeight(),
			node.getMentionCount(),
			node.getTrend().toApiValue(),
			node.getFirstSeen(),
			node.getLastSeen()
		);
	}

	public static GraphNodeResponse forVisualization(UserGraphNode node) {
		return new GraphNodeResponse(
			node.getLabel(),
			node.getGroup().toApiValue(),
			null,
			node.getWeight(),
			node.getMentionCount(),
			null,
			null,
			null
		);
	}
}
