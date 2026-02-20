package be.notification.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import be.common.api.ErrorCode;
import be.notification.domain.GreenroomNotificationSchedule;
import be.notification.domain.NotificationScheduleStatus;
import be.notification.domain.ProcessedEvent;
import be.notification.dto.event.GreenroomDifficultyResolvedEvent;
import be.notification.dto.event.GreenroomNotificationPreferenceUpdatedEvent;
import be.notification.dto.event.GreenroomSessionCompletedEvent;
import be.notification.repository.GreenroomNotificationScheduleRepository;
import be.notification.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GreenroomNotificationScheduleService {

	private static final int DEFAULT_HOUR = 19;
	private static final int DEFAULT_MINUTE = 0;
	private static final String DEFAULT_TIMEZONE = "Asia/Seoul";
	// TODO : 처리 실패 정책에 따라 수정 필요
	private static final long DUE_TOLERANCE_SECONDS = 180L;

	private final GreenroomNotificationScheduleRepository scheduleRepository;
	private final ProcessedEventRepository processedEventRepository;
	private final GreenroomNotificationDispatchService dispatchService;

	@Transactional
	public void handleSessionCompleted(GreenroomSessionCompletedEvent event) {
		// TODO : log 처리
		if (processedEventRepository.existsById(event.eventId())) {
			return;
		}

		Optional<GreenroomNotificationSchedule> existing = scheduleRepository.findByTicketId(event.ticketId());
		GreenroomNotificationSchedule schedule = existing.orElseGet(
			() -> GreenroomNotificationSchedule.create(
				event.userId(),
				event.ticketId(),
				event.occurredAt(),
				nullableInt(event.preferredHour(), DEFAULT_HOUR),
				nullableInt(event.preferredMinute(), DEFAULT_MINUTE),
				nullableString(event.timezone(), DEFAULT_TIMEZONE)
			)
		);

		scheduleRepository.save(schedule);
		processedEventRepository.save(ProcessedEvent.create(event.eventId(), event.eventType()));
	}

	@Transactional
	public void handlePreferenceUpdated(GreenroomNotificationPreferenceUpdatedEvent event) {
		// TODO : log 처리
		if (processedEventRepository.existsById(event.eventId())) {
			return;
		}

		scheduleRepository.findByTicketId(event.ticketId()).ifPresent(schedule -> {
			schedule.updatePreference(
				nullableInt(event.preferredHour(), DEFAULT_HOUR),
				nullableInt(event.preferredMinute(), DEFAULT_MINUTE),
				nullableString(event.timezone(), DEFAULT_TIMEZONE)
			);
			scheduleRepository.save(schedule);
		});
		processedEventRepository.save(ProcessedEvent.create(event.eventId(), event.eventType()));
	}

	@Transactional
	public void handleResolved(GreenroomDifficultyResolvedEvent event) {
		// TODO : log 처리
		if (processedEventRepository.existsById(event.eventId())) {
			return;
		}

		scheduleRepository.findByTicketId(event.ticketId()).ifPresent(schedule -> {
			schedule.markResolved(event.occurredAt());
			scheduleRepository.save(schedule);
		});
		processedEventRepository.save(ProcessedEvent.create(event.eventId(), event.eventType()));
	}

	@Transactional
	public void sendDueSchedules() {
		Instant now = Instant.now();
		processOverdueSchedules(now);

		Instant start = now.minusSeconds(DUE_TOLERANCE_SECONDS);
		Instant end = now.plusSeconds(DUE_TOLERANCE_SECONDS);
		List<GreenroomNotificationSchedule> dueSchedules = scheduleRepository.findByStatusAndNextSendAtBetween(
			NotificationScheduleStatus.ACTIVE,
			start,
			end
		);

		for (GreenroomNotificationSchedule schedule : dueSchedules) {
			dispatchService.sendEmail(schedule);
			schedule.advanceAfterSend(now);
			scheduleRepository.save(schedule);
		}
	}

	private void processOverdueSchedules(Instant now) {
		Instant overdueCutoff = now.minusSeconds(DUE_TOLERANCE_SECONDS);
		List<GreenroomNotificationSchedule> overdueSchedules = scheduleRepository.findByStatusAndNextSendAtBefore(
			NotificationScheduleStatus.ACTIVE,
			overdueCutoff
		);

		for (GreenroomNotificationSchedule schedule : overdueSchedules) {
			dispatchService.markEmailFailedIfUnsent(schedule, ErrorCode.MISSED_AFTER_3_MIN.name());
			schedule.advanceAfterMissed();
			scheduleRepository.save(schedule);
		}
	}

	private int nullableInt(Integer value, int defaultValue) {
		return value == null ? defaultValue : value;
	}

	private String nullableString(String value, String defaultValue) {
		return value == null || value.isBlank() ? defaultValue : value;
	}
}
