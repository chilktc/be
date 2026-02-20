package be.notification.dto.event;

import java.time.Instant;
import java.util.UUID;

public record GreenroomSessionCompletedEvent(
	UUID eventId,
	String eventType,
	Instant occurredAt,
	UUID userId,
	UUID ticketId,
	String timezone,
	Integer preferredHour,
	Integer preferredMinute
) {
}
