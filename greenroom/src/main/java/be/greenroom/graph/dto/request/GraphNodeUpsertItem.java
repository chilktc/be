package be.greenroom.graph.dto.request;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GraphNodeUpsertItem(
	String label,
	String grp,
	Double weight,
	@JsonProperty("mention_count") Integer mentionCount,
	String trend,
	@JsonProperty("first_seen") LocalDateTime firstSeen,
	@JsonProperty("last_seen") LocalDateTime lastSeen
) {
}
