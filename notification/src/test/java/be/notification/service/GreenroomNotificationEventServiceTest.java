package be.notification.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
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

import be.notification.domain.GreenroomNotificationTarget;
import be.notification.domain.GreenroomNotificationUserPreference;
import be.notification.event.GreenroomTicketCreatedEvent;
import be.notification.event.GreenroomTicketResolvedEvent;
import be.notification.event.GreenroomUserNotificationPreferenceUpdatedEvent;
import be.notification.repository.GreenroomNotificationProcessedEventRepository;
import be.notification.repository.GreenroomNotificationTargetRepository;
import be.notification.repository.GreenroomNotificationUserPreferenceRepository;

@ExtendWith(MockitoExtension.class)
class GreenroomNotificationEventServiceTest {

	@Mock
	private GreenroomNotificationTargetRepository targetRepository;
	@Mock
	private GreenroomNotificationUserPreferenceRepository preferenceRepository;
	@Mock
	private GreenroomNotificationProcessedEventRepository processedEventRepository;

	@InjectMocks
	private GreenroomNotificationEventService service;

	@Test
	@DisplayName("ticket created 이벤트는 target을 생성한다")
	void 티켓생성_이벤트_타겟생성() {
		UUID eventId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();
		UUID ticketId = UUID.randomUUID();
		when(processedEventRepository.existsById(eventId)).thenReturn(false);
		when(preferenceRepository.findById(userId)).thenReturn(Optional.empty());
		when(targetRepository.findById(ticketId)).thenReturn(Optional.empty());

		service.handleTicketCreated(new GreenroomTicketCreatedEvent(
			eventId,
			"GREENROOM_TICKET_CREATED",
			LocalDateTime.now(),
			ticketId,
			userId,
			LocalDateTime.of(2026, 3, 1, 10, 0)
		));

		verify(targetRepository).save(any(GreenroomNotificationTarget.class));
		verify(processedEventRepository).save(any());
	}

	@Test
	@DisplayName("이미 처리한 이벤트는 무시한다")
	void 중복이벤트_무시() {
		UUID eventId = UUID.randomUUID();
		when(processedEventRepository.existsById(eventId)).thenReturn(true);

		service.handleTicketResolved(new GreenroomTicketResolvedEvent(
			eventId,
			"GREENROOM_TICKET_RESOLVED",
			LocalDateTime.now(),
			UUID.randomUUID(),
			UUID.randomUUID()
		));

		verify(targetRepository, never()).save(any());
	}

	@Test
	@DisplayName("preference update 이벤트는 사용자 preference와 대상 target enabled를 갱신한다")
	void 알림설정_이벤트_갱신() {
		UUID eventId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();
		when(processedEventRepository.existsById(eventId)).thenReturn(false);
		when(preferenceRepository.findById(userId)).thenReturn(Optional.empty());
		when(targetRepository.findByUserId(userId)).thenReturn(List.of(
			GreenroomNotificationTarget.create(UUID.randomUUID(), userId, LocalDateTime.now(), true)
		));

		service.handleUserPreferenceUpdated(new GreenroomUserNotificationPreferenceUpdatedEvent(
			eventId,
			"GREENROOM_USER_NOTIFICATION_PREFERENCE_UPDATED",
			LocalDateTime.now(),
			userId,
			false
		));

		ArgumentCaptor<GreenroomNotificationUserPreference> prefCaptor = ArgumentCaptor.forClass(
			GreenroomNotificationUserPreference.class
		);
		verify(preferenceRepository).save(prefCaptor.capture());
		assertThat(prefCaptor.getValue().isEnabled()).isFalse();
	}
}
