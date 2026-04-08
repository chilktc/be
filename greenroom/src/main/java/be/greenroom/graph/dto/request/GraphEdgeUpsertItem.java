package be.greenroom.graph.dto.request;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GraphEdgeUpsertItem(
	@JsonProperty("source_label") String sourceLabel,
	@JsonProperty("source_grp") String sourceGrp,
	@JsonProperty("target_label") String targetLabel,
	@JsonProperty("target_grp") String targetGrp,
	Integer weight,
	String relationship,
	@JsonProperty("first_seen") LocalDateTime firstSeen,
	@JsonProperty("last_seen") LocalDateTime lastSeen
) {
}
