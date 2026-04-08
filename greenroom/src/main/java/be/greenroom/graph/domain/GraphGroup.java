package be.greenroom.graph.domain;

import be.common.api.CustomException;
import be.common.api.ErrorCode;

public enum GraphGroup {
	WORK_STRUCTURE,
	LEADERSHIP,
	PEER_RELATIONS,
	CAREER_GROWTH,
	CULTURE_SYSTEM,
	EMOTIONAL_EXHAUSTION;

	public static GraphGroup from(String value) {
		if (value == null || value.isBlank()) {
			throw new CustomException(ErrorCode.GRAPH_INVALID_GROUP);
		}
		try {
			return GraphGroup.valueOf(value.trim().toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new CustomException(ErrorCode.GRAPH_INVALID_GROUP);
		}
	}

	public String toApiValue() {
		return name().toLowerCase();
	}
}
