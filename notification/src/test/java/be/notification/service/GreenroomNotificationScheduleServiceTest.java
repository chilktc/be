package be.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

import be.common.api.CustomException;
import be.common.api.ErrorCode;
import be.notification.domain.GreenroomNotificationSchedule;
import be.notification.domain.NotificationScheduleStatus;
import be.notification.dto.event.GreenroomDifficultyResolvedEvent;
import be.notification.dto.event.GreenroomNotificationPreferenceUpdatedEvent;
import be.notification.dto.event.GreenroomSessionCompletedEvent;
import be.notification.repository.GreenroomNotificationScheduleRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("GreenroomNotificationScheduleService 단위 테스트")
class GreenroomNotificationScheduleServiceTest {

	@Mock
	private GreenroomNotificationScheduleRepository scheduleRepository;

	@Mock
	private GreenroomNotificationDispatchService dispatchService;

	@InjectMocks
	private GreenroomNotificationScheduleService scheduleService;

	@Test
	@DisplayName("세션 완료 이벤트 수신 시 스케줄을 생성한다")
	void handleSessionCompleted_createsSchedule() {
		UUID userId = UUID.randomUUID();
		UUID ticketId = UUID.randomUUID();

		GreenroomSessionCompletedEvent event = new GreenroomSessionCompletedEvent(
			UUID.randomUUID(),
			Instant.parse("2026-03-01T08:00:00Z"),
			userId,
			ticketId,
			17,
			31,
			"Asia/Seoul"
		);

		scheduleService.handleSessionCompleted(event);

		ArgumentCaptor<GreenroomNotificationSchedule> captor = ArgumentCaptor.forClass(GreenroomNotificationSchedule.class);
		verify(scheduleRepository).save(captor.capture());
		GreenroomNotificationSchedule saved = captor.getValue();

		assertThat(saved.getUserId()).isEqualTo(userId);
		assertThat(saved.getTicketId()).isEqualTo(ticketId);
		assertThat(saved.getStatus()).isEqualTo(NotificationScheduleStatus.ACTIVE);
		assertThat(saved.getNextSequence()).isEqualTo(1);
		assertThat(saved.getPreferredHour()).isEqualTo(17);
		assertThat(saved.getPreferredMinute()).isEqualTo(31);
		assertThat(saved.getTimezone()).isEqualTo("Asia/Seoul");
	}

	@Test
	@DisplayName("세션 완료 이벤트 선호값이 null이면 기본값(19:00, Asia/Seoul)을 사용한다")
	void handleSessionCompleted_usesDefaultPreferenceWhenNull() {
		GreenroomSessionCompletedEvent event = new GreenroomSessionCompletedEvent(
			UUID.randomUUID(),
			Instant.parse("2026-03-01T08:00:00Z"),
			UUID.randomUUID(),
			UUID.randomUUID(),
			null,
			null,
			null
		);

		scheduleService.handleSessionCompleted(event);

		ArgumentCaptor<GreenroomNotificationSchedule> captor = ArgumentCaptor.forClass(GreenroomNotificationSchedule.class);
		verify(scheduleRepository).save(captor.capture());
		GreenroomNotificationSchedule saved = captor.getValue();

		assertThat(saved.getPreferredHour()).isEqualTo(19);
		assertThat(saved.getPreferredMinute()).isEqualTo(0);
		assertThat(saved.getTimezone()).isEqualTo("Asia/Seoul");
	}

	@Test
	@DisplayName("완료 전 선호시간 변경 요청은 GREENROOM_SESSION_NOT_COMPLETED를 던진다")
	void handlePreferenceUpdated_throwsNotCompletedWhenScheduleMissing() {
		UUID ticketId = UUID.randomUUID();
		GreenroomNotificationPreferenceUpdatedEvent event = new GreenroomNotificationPreferenceUpdatedEvent(
			UUID.randomUUID(),
			Instant.now(),
			ticketId,
			22,
			15,
			"Asia/Seoul"
		);

		when(scheduleRepository.findByTicketId(ticketId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> scheduleService.handlePreferenceUpdated(event))
			.isInstanceOf(CustomException.class)
			.extracting(e -> ((CustomException) e).getErrorCode())
			.isEqualTo(ErrorCode.GREENROOM_SESSION_NOT_COMPLETED);
	}

	@Test
	@DisplayName("선호시간 변경 이벤트는 스케줄 값을 갱신한다")
	void handlePreferenceUpdated_updatesExistingSchedule() {
		UUID userId = UUID.randomUUID();
		UUID ticketId = UUID.randomUUID();
		GreenroomNotificationSchedule schedule = GreenroomNotificationSchedule.create(
			userId,
			ticketId,
			Instant.parse("2026-03-01T08:00:00Z"),
			19,
			0,
			"Asia/Seoul"
		);

		GreenroomNotificationPreferenceUpdatedEvent event = new GreenroomNotificationPreferenceUpdatedEvent(
			UUID.randomUUID(),
			Instant.parse("2026-03-01T09:00:00Z"),
			ticketId,
			22,
			15,
			"Asia/Seoul"
		);

		when(scheduleRepository.findByTicketId(ticketId)).thenReturn(Optional.of(schedule));

		scheduleService.handlePreferenceUpdated(event);

		verify(scheduleRepository).save(schedule);
		assertThat(schedule.getPreferredHour()).isEqualTo(22);
		assertThat(schedule.getPreferredMinute()).isEqualTo(15);
	}

	@Test
	@DisplayName("완료 전 해결 요청은 GREENROOM_SESSION_NOT_COMPLETED를 던진다")
	void handleResolved_throwsNotCompletedWhenScheduleMissing() {
		UUID ticketId = UUID.randomUUID();
		GreenroomDifficultyResolvedEvent event = new GreenroomDifficultyResolvedEvent(
			UUID.randomUUID(),
			Instant.now(),
			ticketId
		);

		when(scheduleRepository.findByTicketId(ticketId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> scheduleService.handleResolved(event))
			.isInstanceOf(CustomException.class)
			.extracting(e -> ((CustomException) e).getErrorCode())
			.isEqualTo(ErrorCode.GREENROOM_SESSION_NOT_COMPLETED);
	}

	@Test
	@DisplayName("해결 이벤트는 스케줄을 RESOLVED 상태로 변경한다")
	void handleResolved_marksScheduleResolved() {
		UUID ticketId = UUID.randomUUID();
		GreenroomNotificationSchedule schedule = GreenroomNotificationSchedule.create(
			UUID.randomUUID(),
			ticketId,
			Instant.parse("2026-03-01T08:00:00Z"),
			19,
			0,
			"Asia/Seoul"
		);

		GreenroomDifficultyResolvedEvent event = new GreenroomDifficultyResolvedEvent(
			UUID.randomUUID(),
			Instant.parse("2026-03-02T08:00:00Z"),
			ticketId
		);

		when(scheduleRepository.findByTicketId(ticketId)).thenReturn(Optional.of(schedule));

		scheduleService.handleResolved(event);

		verify(scheduleRepository).save(schedule);
		assertThat(schedule.getStatus()).isEqualTo(NotificationScheduleStatus.RESOLVED);
		assertThat(schedule.getResolvedAt()).isEqualTo(Instant.parse("2026-03-02T08:00:00Z"));
	}

	@Test
	@DisplayName("due 스케줄은 이메일 전송 후 nextSequence를 증가시킨다")
	void sendDueSchedules_sendsDueSchedulesAndAdvancesSequence() {
		GreenroomNotificationSchedule due = GreenroomNotificationSchedule.create(
			UUID.randomUUID(),
			UUID.randomUUID(),
			Instant.parse("2026-03-01T08:00:00Z"),
			19,
			0,
			"Asia/Seoul"
		);

		when(scheduleRepository.findByStatusAndNextSendAtBefore(eq(NotificationScheduleStatus.ACTIVE), any()))
			.thenReturn(List.of());
		when(scheduleRepository.findByStatusAndNextSendAtBetween(eq(NotificationScheduleStatus.ACTIVE), any(), any()))
			.thenReturn(List.of(due));

		scheduleService.sendDueSchedules();

		verify(dispatchService).sendEmail(due);
		verify(scheduleRepository).save(due);
		assertThat(due.getNextSequence()).isEqualTo(2);
		assertThat(due.getLastSentAt()).isNotNull();
	}

	@Test
	@DisplayName("overdue 스케줄은 실패 처리 후 nextSequence를 증가시킨다")
	void sendDueSchedules_marksOverdueAsFailedAndAdvancesSequence() {
		GreenroomNotificationSchedule overdue = GreenroomNotificationSchedule.create(
			UUID.randomUUID(),
			UUID.randomUUID(),
			Instant.parse("2026-03-01T08:00:00Z"),
			19,
			0,
			"Asia/Seoul"
		);

		when(scheduleRepository.findByStatusAndNextSendAtBefore(eq(NotificationScheduleStatus.ACTIVE), any()))
			.thenReturn(List.of(overdue));
		when(scheduleRepository.findByStatusAndNextSendAtBetween(eq(NotificationScheduleStatus.ACTIVE), any(), any()))
			.thenReturn(List.of());

		scheduleService.sendDueSchedules();

		verify(dispatchService).markEmailFailedIfUnsent(overdue, "MISSED_AFTER_3_MIN");
		verify(scheduleRepository).save(overdue);
		assertThat(overdue.getNextSequence()).isEqualTo(2);
	}

	@Test
	@DisplayName("처리 대상이 없으면 dispatch 서비스와 상호작용하지 않는다")
	void sendDueSchedules_doesNothingWhenNoDueAndNoOverdue() {
		when(scheduleRepository.findByStatusAndNextSendAtBefore(eq(NotificationScheduleStatus.ACTIVE), any()))
			.thenReturn(List.of());
		when(scheduleRepository.findByStatusAndNextSendAtBetween(eq(NotificationScheduleStatus.ACTIVE), any(), any()))
			.thenReturn(List.of());

		scheduleService.sendDueSchedules();

		verifyNoInteractions(dispatchService);
	}
}
