package be.notification.event;

import java.util.UUID;

public record GreenroomNotificationDispatchRequestEvent(
	UUID ticketId,
	UUID userId,
	int sequence
) {
}
