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
	void 세션완료_이벤트_수신시__스케줄_생성() {
		// given
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

		// when
		scheduleService.handleSessionCompleted(event);

		// then
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
	void 세션완료_이벤트_선호값이_null이면__기본값_적용() {
		// given
		GreenroomSessionCompletedEvent event = new GreenroomSessionCompletedEvent(
			UUID.randomUUID(),
			Instant.parse("2026-03-01T08:00:00Z"),
			UUID.randomUUID(),
			UUID.randomUUID(),
			null,
			null,
			null
		);

		// when
		scheduleService.handleSessionCompleted(event);

		// then
		ArgumentCaptor<GreenroomNotificationSchedule> captor = ArgumentCaptor.forClass(GreenroomNotificationSchedule.class);
		verify(scheduleRepository).save(captor.capture());
		GreenroomNotificationSchedule saved = captor.getValue();

		assertThat(saved.getPreferredHour()).isEqualTo(19);
		assertThat(saved.getPreferredMinute()).isEqualTo(0);
		assertThat(saved.getTimezone()).isEqualTo("Asia/Seoul");
	}

	@Test
	@DisplayName("완료 전 선호시간 변경 요청은 GREENROOM_SESSION_NOT_COMPLETED를 던진다")
	void 완료전_선호시간_변경요청시__미완료_예외_발생() {
		// given
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

		// when & then
		assertThatThrownBy(() -> scheduleService.handlePreferenceUpdated(event))
			.isInstanceOf(CustomException.class)
			.extracting(e -> ((CustomException) e).getErrorCode())
			.isEqualTo(ErrorCode.GREENROOM_SESSION_NOT_COMPLETED);
	}

	@Test
	@DisplayName("선호시간 변경 이벤트는 스케줄 값을 갱신한다")
	void 선호시간_변경_이벤트_수신시__스케줄_값_갱신() {
		// given
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

		// when
		scheduleService.handlePreferenceUpdated(event);

		// then
		verify(scheduleRepository).save(schedule);
		assertThat(schedule.getPreferredHour()).isEqualTo(22);
		assertThat(schedule.getPreferredMinute()).isEqualTo(15);
	}

	@Test
	@DisplayName("완료 전 해결 요청은 GREENROOM_SESSION_NOT_COMPLETED를 던진다")
	void 완료전_해결요청시__미완료_예외_발생() {
		// given
		UUID ticketId = UUID.randomUUID();
		GreenroomDifficultyResolvedEvent event = new GreenroomDifficultyResolvedEvent(
			UUID.randomUUID(),
			Instant.now(),
			ticketId
		);

		when(scheduleRepository.findByTicketId(ticketId)).thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> scheduleService.handleResolved(event))
			.isInstanceOf(CustomException.class)
			.extracting(e -> ((CustomException) e).getErrorCode())
			.isEqualTo(ErrorCode.GREENROOM_SESSION_NOT_COMPLETED);
	}

	@Test
	@DisplayName("해결 이벤트는 스케줄을 RESOLVED 상태로 변경한다")
	void 해결_이벤트_수신시__스케줄_RESOLVED_변경() {
		// given
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

		// when
		scheduleService.handleResolved(event);

		// then
		verify(scheduleRepository).save(schedule);
		assertThat(schedule.getStatus()).isEqualTo(NotificationScheduleStatus.RESOLVED);
		assertThat(schedule.getResolvedAt()).isEqualTo(Instant.parse("2026-03-02T08:00:00Z"));
	}

	@Test
	@DisplayName("due 스케줄은 이메일 전송 후 nextSequence를 증가시킨다")
	void 듀_스케줄_처리시__이메일전송후_다음시퀀스_증가() {
		// given
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

		// when
		scheduleService.sendDueSchedules();

		// then
		verify(dispatchService).sendEmail(due);
		verify(scheduleRepository).save(due);
		assertThat(due.getNextSequence()).isEqualTo(2);
		assertThat(due.getLastSentAt()).isNotNull();
	}

	@Test
	@DisplayName("overdue 스케줄은 실패 처리 후 nextSequence를 증가시킨다")
	void 오버듀_스케줄_처리시__실패처리후_다음시퀀스_증가() {
		// given
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

		// when
		scheduleService.sendDueSchedules();

		// then
		verify(dispatchService).markEmailFailedIfUnsent(overdue, "MISSED_AFTER_3_MIN");
		verify(scheduleRepository).save(overdue);
		assertThat(overdue.getNextSequence()).isEqualTo(2);
	}

	@Test
	@DisplayName("처리 대상이 없으면 dispatch 서비스와 상호작용하지 않는다")
	void 처리대상_없으면__디스패치_호출없음() {
		// given
		when(scheduleRepository.findByStatusAndNextSendAtBefore(eq(NotificationScheduleStatus.ACTIVE), any()))
			.thenReturn(List.of());
		when(scheduleRepository.findByStatusAndNextSendAtBetween(eq(NotificationScheduleStatus.ACTIVE), any(), any()))
			.thenReturn(List.of());

		// when
		scheduleService.sendDueSchedules();

		// then
		verifyNoInteractions(dispatchService);
	}
}
