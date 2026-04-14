package be.greenroom.ai.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import be.greenroom.ai.domain.Podcast;

public record PodcastResponse(
	UUID id,
	String sessionId,
	String imageUrl,
	String text,
	LocalDateTime createdAt
) {
	public static PodcastResponse from(Podcast podcast) {
		return new PodcastResponse(
			podcast.getId(),
			podcast.getSessionId(),
			podcast.getImageUrl(),
			podcast.getText(),
			podcast.getCreatedAt()
		);
	}
}
