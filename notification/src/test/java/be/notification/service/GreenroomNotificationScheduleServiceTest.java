package be.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import be.notification.domain.GreenroomNotificationSchedule;
import be.notification.domain.NotificationScheduleStatus;
import be.notification.domain.ProcessedEvent;
import be.notification.dto.event.GreenroomDifficultyResolvedEvent;
import be.notification.dto.event.GreenroomNotificationPreferenceUpdatedEvent;
import be.notification.dto.event.GreenroomSessionCompletedEvent;
import be.notification.repository.GreenroomNotificationScheduleRepository;
import be.notification.repository.ProcessedEventRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("GreenroomNotificationScheduleService 단위 테스트")
class GreenroomNotificationScheduleServiceTest {

	@Mock
	private GreenroomNotificationScheduleRepository scheduleRepository;

	@Mock
	private ProcessedEventRepository processedEventRepository;

	@Mock
	private GreenroomNotificationDispatchService dispatchService;

	@InjectMocks
	private GreenroomNotificationScheduleService scheduleService;

	@Test
	@DisplayName("세션 완료 이벤트가 신규이면 스케줄을 생성하고 processed_event를 저장한다")
	void handleSessionCompleted_createsScheduleAndRecordsProcessedEvent_whenNew() {
		UUID eventId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();
		UUID ticketId = UUID.randomUUID();

		GreenroomSessionCompletedEvent event = new GreenroomSessionCompletedEvent(
			eventId,
			"GREENROOM_SESSION_COMPLETED",
			Instant.parse("2026-03-01T08:00:00Z"),
			userId,
			ticketId,
			"Asia/Seoul",
			17,
			31
		);

		when(processedEventRepository.existsById(eventId)).thenReturn(false);
		when(scheduleRepository.findByTicketId(ticketId)).thenReturn(Optional.empty());

		scheduleService.handleSessionCompleted(event);

		ArgumentCaptor<GreenroomNotificationSchedule> scheduleCaptor =
			ArgumentCaptor.forClass(GreenroomNotificationSchedule.class);
		verify(scheduleRepository).save(scheduleCaptor.capture());
		GreenroomNotificationSchedule saved = scheduleCaptor.getValue();

		assertThat(saved.getUserId()).isEqualTo(userId);
		assertThat(saved.getTicketId()).isEqualTo(ticketId);
		assertThat(saved.getStatus()).isEqualTo(NotificationScheduleStatus.ACTIVE);
		assertThat(saved.getNextSequence()).isEqualTo(1);
		assertThat(saved.getPreferredHour()).isEqualTo(17);
		assertThat(saved.getPreferredMinute()).isEqualTo(31);
		assertThat(saved.getTimezone()).isEqualTo("Asia/Seoul");

		ArgumentCaptor<ProcessedEvent> processedCaptor = ArgumentCaptor.forClass(ProcessedEvent.class);
		verify(processedEventRepository).save(processedCaptor.capture());
		assertThat(processedCaptor.getValue().getEventId()).isEqualTo(eventId);
	}

	@Test
	@DisplayName("세션 완료 이벤트가 중복이면 아무 작업도 수행하지 않는다")
	void handleSessionCompleted_returnsImmediately_whenDuplicateEvent() {
		UUID eventId = UUID.randomUUID();
		GreenroomSessionCompletedEvent event = new GreenroomSessionCompletedEvent(
			eventId,
			"GREENROOM_SESSION_COMPLETED",
			Instant.now(),
			UUID.randomUUID(),
			UUID.randomUUID(),
			"Asia/Seoul",
			19,
			0
		);

		when(processedEventRepository.existsById(eventId)).thenReturn(true);

		scheduleService.handleSessionCompleted(event);

		verify(scheduleRepository, never()).findByTicketId(any());
		verify(scheduleRepository, never()).save(any());
		verify(processedEventRepository, never()).save(any());
	}

	@Test
	@DisplayName("세션 완료 이벤트에 선호 시간이 비어있으면 기본값(19:00, Asia/Seoul)을 사용한다")
	void handleSessionCompleted_usesDefaultPreference_whenEventPreferenceIsNull() {
		UUID eventId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();
		UUID ticketId = UUID.randomUUID();

		GreenroomSessionCompletedEvent event = new GreenroomSessionCompletedEvent(
			eventId,
			"GREENROOM_SESSION_COMPLETED",
			Instant.parse("2026-03-01T08:00:00Z"),
			userId,
			ticketId,
			null,
			null,
			null
		);

		when(processedEventRepository.existsById(eventId)).thenReturn(false);
		when(scheduleRepository.findByTicketId(ticketId)).thenReturn(Optional.empty());

		scheduleService.handleSessionCompleted(event);

		ArgumentCaptor<GreenroomNotificationSchedule> scheduleCaptor =
			ArgumentCaptor.forClass(GreenroomNotificationSchedule.class);
		verify(scheduleRepository).save(scheduleCaptor.capture());
		GreenroomNotificationSchedule saved = scheduleCaptor.getValue();

		assertThat(saved.getPreferredHour()).isEqualTo(19);
		assertThat(saved.getPreferredMinute()).isEqualTo(0);
		assertThat(saved.getTimezone()).isEqualTo("Asia/Seoul");
		verify(processedEventRepository).save(any(ProcessedEvent.class));
	}
}
