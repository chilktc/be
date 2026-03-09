package be.notification.scheduler;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import be.notification.domain.GreenroomNotificationTarget;
import be.notification.repository.GreenroomNotificationTargetRepository;
import be.notification.service.GreenroomNotificationDispatchService;

@ExtendWith(MockitoExtension.class)
class GreenroomNotificationSchedulerTest {

	@Mock
	private GreenroomNotificationTargetRepository targetRepository;
	@Mock
	private GreenroomNotificationDispatchService dispatchService;

	@InjectMocks
	private GreenroomNotificationScheduler scheduler;

	@Test
	@DisplayName("발송 성공 시 nextSequence를 증가시킨다")
	void 발송성공시_시퀀스증가() {
		// given
		GreenroomNotificationTarget target = GreenroomNotificationTarget.create(
			UUID.randomUUID(),
			UUID.randomUUID(),
			LocalDateTime.of(2026, 3, 1, 10, 0),
			true
		);
		when(targetRepository.findByResolvedFalseAndEnabledTrueAndNextSendAtLessThanEqual(any(Instant.class)))
			.thenReturn(List.of(target));
		when(dispatchService.sendEmail(target.getUserId(), target.getTicketId(), target.getNextSequence()))
			.thenReturn(true);

		// when
		scheduler.run();

		// then
		ArgumentCaptor<GreenroomNotificationTarget> captor = ArgumentCaptor.forClass(GreenroomNotificationTarget.class);
		verify(targetRepository).save(captor.capture());
		assertThat(captor.getValue().getNextSequence()).isEqualTo(2);
	}

	@Test
	@DisplayName("발송 실패 시 nextSequence를 유지한다")
	void 발송실패시_시퀀스유지() {
		// given
		GreenroomNotificationTarget target = GreenroomNotificationTarget.create(
			UUID.randomUUID(),
			UUID.randomUUID(),
			LocalDateTime.of(2026, 3, 1, 10, 0),
			true
		);
		when(targetRepository.findByResolvedFalseAndEnabledTrueAndNextSendAtLessThanEqual(any(Instant.class)))
			.thenReturn(List.of(target));
		when(dispatchService.sendEmail(target.getUserId(), target.getTicketId(), target.getNextSequence()))
			.thenReturn(false);

		// when
		scheduler.run();

		// then
		verify(targetRepository, never()).save(any());
	}
}
