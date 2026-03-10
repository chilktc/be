package be.notification.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import be.notification.domain.GreenroomNotificationProcessedEvent;
import be.notification.domain.GreenroomNotificationTarget;
import be.notification.domain.GreenroomNotificationUserPreference;
import be.notification.event.GreenroomTicketCreatedEvent;
import be.notification.event.GreenroomTicketResolvedEvent;
import be.notification.event.GreenroomUserNotificationPreferenceUpdatedEvent;
import be.notification.repository.GreenroomNotificationProcessedEventRepository;
import be.notification.repository.GreenroomNotificationTargetRepository;
import be.notification.repository.GreenroomNotificationUserPreferenceRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GreenroomNotificationEventService {

	private final GreenroomNotificationTargetRepository targetRepository;
	private final GreenroomNotificationUserPreferenceRepository preferenceRepository;
	private final GreenroomNotificationProcessedEventRepository processedEventRepository;

	@Transactional
	public void handleTicketCreated(GreenroomTicketCreatedEvent event) {
		if (isAlreadyProcessed(event.eventId())) {
			return;
		}
		boolean enabled = preferenceRepository.findById(event.userId())
			.map(GreenroomNotificationUserPreference::isEnabled)
			.orElse(true);
		GreenroomNotificationTarget target = targetRepository.findById(event.ticketId())
			.orElseGet(() -> GreenroomNotificationTarget.create(
				event.ticketId(),
				event.userId(),
				event.ticketCreatedAt(),
				enabled
			));
		target.changeEnabled(enabled);
		targetRepository.save(target);
		saveProcessed(event.eventId(), event.eventType());
	}

	@Transactional
	public void handleTicketResolved(GreenroomTicketResolvedEvent event) {
		if (isAlreadyProcessed(event.eventId())) {
			return;
		}
		targetRepository.findById(event.ticketId()).ifPresent(target -> {
			target.resolve();
			targetRepository.save(target);
		});
		saveProcessed(event.eventId(), event.eventType());
	}

	// TODO : 최초 회원가입 시 알림 설정 정보 추가 될 수 있음.
	@Transactional
	public void handleUserPreferenceUpdated(GreenroomUserNotificationPreferenceUpdatedEvent event) {
		if (isAlreadyProcessed(event.eventId())) {
			return;
		}
		GreenroomNotificationUserPreference preference = preferenceRepository.findById(event.userId())
			.orElseGet(() -> GreenroomNotificationUserPreference.create(event.userId(), true));
		preference.changeEnabled(event.enabled());
		preferenceRepository.save(preference);
		targetRepository.updateEnabledByUserId(event.userId(), event.enabled());

		saveProcessed(event.eventId(), event.eventType());
	}

	private boolean isAlreadyProcessed(UUID eventId) {
		return processedEventRepository.existsById(eventId);
	}

	private void saveProcessed(UUID eventId, String eventType) {
		processedEventRepository.save(GreenroomNotificationProcessedEvent.create(eventId, eventType));
	}
}
