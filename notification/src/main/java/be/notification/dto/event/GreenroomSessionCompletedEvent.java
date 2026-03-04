package be.notification.dto.event;

import java.time.Instant;
import java.util.UUID;

public record GreenroomSessionCompletedEvent(
	UUID eventId,
	Instant occurredAt,
	UUID userId,
	UUID ticketId,
	Integer preferredHour,
	Integer preferredMinute,
	String timezone
) {
}
