package be.notification.dto.event;

import java.time.Instant;
import java.util.UUID;

public record GreenroomNotificationPreferenceUpdatedEvent(
	UUID eventId,
	String eventType,
	Instant occurredAt,
	UUID userId,
	UUID ticketId,
	Integer preferredHour,
	Integer preferredMinute,
	String timezone
) {
}
