package be.auth.notification.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record GreenroomUserNotificationPreferenceUpdatedEvent(
	UUID eventId,
	String eventType,
	LocalDateTime occurredAt,
	UUID userId,
	boolean enabled
) {
}
