package be.greenroom.graph.dto.request;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record PutGraphNodesRequest(
	@JsonProperty("user_id") @NotNull UUID userId,
	@NotNull String type,
	@Valid @NotNull GraphCumulativePayload data
) {
}
