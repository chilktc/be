package be.greenroom.ai.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import be.greenroom.ai.domain.MindFrequency;

public record MindFrequencyWithPodcastResponse(
	UUID id,
	List<String> keywords,
	String description,
	LocalDateTime createdAt,
	String imageUrl
) {
	public static MindFrequencyWithPodcastResponse from(MindFrequency mindFrequency, String imageUrl) {
		return new MindFrequencyWithPodcastResponse(
			mindFrequency.getId(),
			mindFrequency.getKeywordsAsList(),
			mindFrequency.getDescription(),
			mindFrequency.getCreatedAt(),
			imageUrl
		);
	}
}
