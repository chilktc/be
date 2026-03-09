package be.greenroom.notification.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import be.greenroom.notification.domain.UserNotificationPreference;
import be.greenroom.notification.event.GreenroomNotificationEventType;
import be.greenroom.notification.event.GreenroomUserNotificationPreferenceUpdatedEvent;
import be.greenroom.notification.repository.UserNotificationPreferenceRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserNotificationPreferenceService {

	private final UserNotificationPreferenceRepository preferenceRepository;
	private final GreenroomNotificationEventPublisher eventPublisher;

	@Transactional
	public void toggle(UUID userId) {
		UserNotificationPreference preference = preferenceRepository.findById(userId)
			.orElseGet(() -> UserNotificationPreference.create(userId, true));
		boolean enabled = !preference.isEnabled();
		preference.changeEnabled(enabled);
		preferenceRepository.save(preference);
		GreenroomUserNotificationPreferenceUpdatedEvent event = new GreenroomUserNotificationPreferenceUpdatedEvent(
			UUID.randomUUID(),
			GreenroomNotificationEventType.GREENROOM_USER_NOTIFICATION_PREFERENCE_UPDATED.name(),
			LocalDateTime.now(),
			userId,
			enabled
		);
		eventPublisher.publish(userId.toString(), event);
	}
}
