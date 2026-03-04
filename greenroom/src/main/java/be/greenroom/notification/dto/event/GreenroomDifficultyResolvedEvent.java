package be.greenroom.notification.dto.event;

import java.time.Instant;
import java.util.UUID;

public record GreenroomDifficultyResolvedEvent(
	UUID eventId,
	Instant occurredAt,
	UUID ticketId
) {
}
