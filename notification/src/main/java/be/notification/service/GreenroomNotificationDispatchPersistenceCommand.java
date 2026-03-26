package be.notification.service;

import java.time.Instant;
import java.util.UUID;

import be.notification.domain.GreenroomTemplateCode;

public record GreenroomNotificationDispatchPersistenceCommand(
	UUID ticketId,
	UUID userId,
	int sequence,
	String idempotencyKey,
	GreenroomTemplateCode templateCode,
	Instant sentAt
) {
}
