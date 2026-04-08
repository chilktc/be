package be.greenroom.graph.domain;

import be.common.api.CustomException;
import be.common.api.ErrorCode;

public enum GraphTrend {
	INCREASING,
	STABLE,
	DECREASING;

	public static GraphTrend from(String value) {
		if (value == null || value.isBlank()) {
			throw new CustomException(ErrorCode.GRAPH_INVALID_TREND);
		}
		try {
			return GraphTrend.valueOf(value.trim().toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new CustomException(ErrorCode.GRAPH_INVALID_TREND);
		}
	}

	public String toApiValue() {
		return name().toLowerCase();
	}
}
