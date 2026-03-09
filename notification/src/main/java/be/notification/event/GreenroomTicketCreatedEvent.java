package be.notification.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record GreenroomTicketCreatedEvent(
	UUID eventId,
	String eventType,
	LocalDateTime occurredAt,
	UUID ticketId,
	UUID userId,
	LocalDateTime ticketCreatedAt
) {
}
