package be.greenroom.ai.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import be.greenroom.ai.domain.MindFrequency;

public record MindFrequencyResponse(
	UUID id,
	List<String> keywords,
	String description,
	LocalDateTime createdAt
) {
	public static MindFrequencyResponse from(MindFrequency mindFrequency) {
		return new MindFrequencyResponse(
			mindFrequency.getId(),
			mindFrequency.getKeywordsAsList(),
			mindFrequency.getDescription(),
			mindFrequency.getCreatedAt()
		);
	}
}
