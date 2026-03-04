package be.notification.dto.event;

import java.time.Instant;
import java.util.UUID;

public record GreenroomNotificationPreferenceUpdatedEvent(
	UUID eventId,
	Instant occurredAt,
	UUID ticketId,
	Integer preferredHour,
	Integer preferredMinute,
	String timezone
) {
}
