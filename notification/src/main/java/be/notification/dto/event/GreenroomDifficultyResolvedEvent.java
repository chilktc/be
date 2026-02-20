package be.notification.dto.event;

import java.time.Instant;
import java.util.UUID;

public record GreenroomDifficultyResolvedEvent(
	UUID eventId,
	String eventType,
	Instant occurredAt,
	UUID userId,
	UUID ticketId,
	String resolvedBy
) {
}
