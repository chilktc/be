package be.greenroom.notification.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record GreenroomTicketResolvedEvent(
	UUID eventId,
	String eventType,
	LocalDateTime occurredAt,
	UUID ticketId,
	UUID userId
) {
}
