package be.greenroom.notification.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;

import be.greenroom.notification.dto.event.GreenroomDifficultyResolvedEvent;
import be.greenroom.notification.dto.event.GreenroomNotificationPreferenceUpdatedEvent;
import be.greenroom.notification.dto.event.GreenroomSessionCompletedEvent;
import be.greenroom.notification.dto.request.ResolveDifficultyRequest;
import be.greenroom.notification.dto.request.UpdateNotificationPreferenceRequest;
import be.greenroom.notification.event.GreenroomEventType;
import be.greenroom.notification.producer.GreenroomNotificationEventProducer;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GreenroomNotificationCommandService {

	private static final int DEFAULT_PREFERRED_HOUR = 19;
	private static final int DEFAULT_PREFERRED_MINUTE = 0;
	private static final String DEFAULT_TIMEZONE = "Asia/Seoul";

	private final GreenroomNotificationEventProducer producer;

	public void completeTicket(UUID userId, UUID ticketId, UpdateNotificationPreferenceRequest request) {
		int preferredHour = request == null ? DEFAULT_PREFERRED_HOUR : request.preferredHour();
		int preferredMinute = request == null ? DEFAULT_PREFERRED_MINUTE : request.preferredMinute();
		String timezone = request == null ? DEFAULT_TIMEZONE : request.timezone();

		GreenroomSessionCompletedEvent event = new GreenroomSessionCompletedEvent(
			UUID.randomUUID(),
			GreenroomEventType.GREENROOM_SESSION_COMPLETED.name(),
			Instant.now(),
			userId,
			ticketId,
			timezone,
			preferredHour,
			preferredMinute
		);
		producer.publish(event);
	}

	public void updateNotificationPreference(
		UUID userId,
		UUID ticketId,
		UpdateNotificationPreferenceRequest request
	) {
		GreenroomNotificationPreferenceUpdatedEvent event = new GreenroomNotificationPreferenceUpdatedEvent(
			UUID.randomUUID(),
			GreenroomEventType.GREENROOM_NOTIFICATION_PREFERENCE_UPDATED.name(),
			Instant.now(),
			userId,
			ticketId,
			request.preferredHour(),
			request.preferredMinute(),
			request.timezone()
		);
		producer.publish(event);
	}

	public void resolveDifficulty(UUID userId, UUID ticketId, ResolveDifficultyRequest request) {
		GreenroomDifficultyResolvedEvent event = new GreenroomDifficultyResolvedEvent(
			UUID.randomUUID(),
			GreenroomEventType.GREENROOM_DIFFICULTY_RESOLVED.name(),
			Instant.now(),
			userId,
			ticketId,
			request.resolvedBy()
		);
		producer.publish(event);
	}
}
