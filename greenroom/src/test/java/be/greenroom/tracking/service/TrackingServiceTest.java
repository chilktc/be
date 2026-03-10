package be.greenroom.tracking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import be.common.api.CustomException;
import be.common.api.ErrorCode;
import be.greenroom.notification.event.GreenroomTicketResolvedEvent;
import be.greenroom.notification.service.GreenroomNotificationEventPublisher;
import be.greenroom.ticket.domain.Ticket;
import be.greenroom.ticket.repository.TicketRepository;
import be.greenroom.tracking.domain.ResolvedHelpType;
import be.greenroom.tracking.domain.ResolvedStateType;
import be.greenroom.tracking.domain.Tracking;
import be.greenroom.tracking.domain.TrackingStatus;
import be.greenroom.tracking.domain.UnresolvedBlockerType;
import be.greenroom.tracking.domain.UnresolvedNeedType;
import be.greenroom.tracking.dto.request.CreateTrackingRequest;
import be.greenroom.tracking.dto.response.TrackingHistoryItemResponse;
import be.greenroom.tracking.repository.TrackingRepository;

@ExtendWith(MockitoExtension.class)
class TrackingServiceTest {

	@Mock
	private TicketRepository ticketRepository;
	@Mock
	private TrackingRepository trackingRepository;
	@Mock
	private GreenroomNotificationEventPublisher eventPublisher;
	@Spy
	private TrackingRequestValidator trackingRequestValidator;

	@InjectMocks
	private TrackingService trackingService;

	@Test
	@DisplayName("해결 트래킹 등록 시 해결 이벤트를 발행한다")
	void 해결트래킹_이벤트발행() {
		// given
		UUID userId = UUID.randomUUID();
		UUID ticketId = UUID.randomUUID();
		Ticket ticket = Ticket.create(userId, "n", "s", "t", "a", "c");
		ReflectionTestUtils.setField(ticket, "id", ticketId);
		when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
		when(trackingRepository.existsByTicketIdAndStatus(ticketId, TrackingStatus.RESOLVED)).thenReturn(false);
		when(trackingRepository.save(any(Tracking.class))).thenAnswer(invocation -> invocation.getArgument(0));
		CreateTrackingRequest request = new CreateTrackingRequest(
			TrackingStatus.RESOLVED,
			ResolvedHelpType.DIALOGUE_AND_EXPRESSION,
			null,
			ResolvedStateType.FULLY_DONE,
			null,
			null,
			null,
			"끝남"
		);

		// when
		trackingService.create(userId, ticketId, request);

		// then
		ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
		verify(eventPublisher).publish(org.mockito.ArgumentMatchers.eq(userId.toString()), eventCaptor.capture());
		assertThat(eventCaptor.getValue()).isInstanceOf(GreenroomTicketResolvedEvent.class);
	}

	@Test
	@DisplayName("미해결 트래킹 등록 시 해결 이벤트를 발행하지 않는다")
	void 미해결트래킹_이벤트미발행() {
		// given
		UUID userId = UUID.randomUUID();
		UUID ticketId = UUID.randomUUID();
		Ticket ticket = Ticket.create(userId, "n", "s", "t", "a", "c");
		ReflectionTestUtils.setField(ticket, "id", ticketId);
		when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
		when(trackingRepository.existsByTicketIdAndStatus(ticketId, TrackingStatus.RESOLVED)).thenReturn(false);
		when(trackingRepository.save(any(Tracking.class))).thenAnswer(invocation -> invocation.getArgument(0));
		CreateTrackingRequest request = new CreateTrackingRequest(
			TrackingStatus.UNRESOLVED,
			null,
			null,
			null,
			UnresolvedBlockerType.EXECUTION_IS_HARD,
			null,
			UnresolvedNeedType.SMALL_EXECUTABLE_ACTION,
			null
		);

		// when
		trackingService.create(userId, ticketId, request);

		// then
		verify(eventPublisher, never()).publish(any(), any());
	}

	@Test
	@DisplayName("해결 상태에서 미해결 필드가 있으면 예외가 발생한다")
	void 해결요청_필드검증예외() {
		// given
		UUID userId = UUID.randomUUID();
		UUID ticketId = UUID.randomUUID();
		Ticket ticket = Ticket.create(userId, "n", "s", "t", "a", "c");
		ReflectionTestUtils.setField(ticket, "id", ticketId);
		when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
		when(trackingRepository.existsByTicketIdAndStatus(ticketId, TrackingStatus.RESOLVED)).thenReturn(false);
		CreateTrackingRequest request = new CreateTrackingRequest(
			TrackingStatus.RESOLVED,
			ResolvedHelpType.DIALOGUE_AND_EXPRESSION,
			null,
			ResolvedStateType.FULLY_DONE,
			UnresolvedBlockerType.EXECUTION_IS_HARD,
			null,
			UnresolvedNeedType.SMALL_EXECUTABLE_ACTION,
			null
		);

		// when then
		assertThatThrownBy(() -> trackingService.create(userId, ticketId, request))
			.isInstanceOf(CustomException.class);
	}

	@Test
	@DisplayName("이미 해결된 티켓이면 트래킹 생성 시 예외가 발생한다")
	void 이미해결된티켓_트래킹생성예외() {
		// given
		UUID userId = UUID.randomUUID();
		UUID ticketId = UUID.randomUUID();
		Ticket ticket = Ticket.create(userId, "n", "s", "t", "a", "c");
		ReflectionTestUtils.setField(ticket, "id", ticketId);
		when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
		when(trackingRepository.existsByTicketIdAndStatus(ticketId, TrackingStatus.RESOLVED)).thenReturn(true);
		CreateTrackingRequest request = new CreateTrackingRequest(
			TrackingStatus.UNRESOLVED,
			null,
			null,
			null,
			UnresolvedBlockerType.EXECUTION_IS_HARD,
			null,
			UnresolvedNeedType.SMALL_EXECUTABLE_ACTION,
			null
		);

		// when then
		assertThatThrownBy(() -> trackingService.create(userId, ticketId, request))
			.isInstanceOf(CustomException.class)
			.extracting(e -> ((CustomException)e).getErrorCode())
			.isEqualTo(ErrorCode.ALREADY_RESOLVED_TICKET);
	}

	@Test
	@DisplayName("트래킹 조회 시 최신순과 D+일차를 계산한다")
	void 트래킹조회_최신순_D데이계산() {
		// given
		UUID userId = UUID.randomUUID();
		UUID ticketId = UUID.randomUUID();
		Ticket ticket = Ticket.create(userId, "n", "s", "t", "a", "c");
		ReflectionTestUtils.setField(ticket, "id", ticketId);
		ReflectionTestUtils.setField(ticket, "createdAt", LocalDateTime.of(2026, 3, 1, 10, 0));
		when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

		Tracking first = Tracking.builder()
			.ticketId(ticketId)
			.userId(userId)
			.status(TrackingStatus.UNRESOLVED)
			.unresolvedBlockerType(UnresolvedBlockerType.DONT_KNOW_WHERE_TO_START)
			.unresolvedNeedType(UnresolvedNeedType.ORGANIZE_OBJECTIVELY)
			.note("정리필요")
			.build();
		ReflectionTestUtils.setField(first, "createdAt", LocalDateTime.of(2026, 3, 8, 8, 30));

		Tracking second = Tracking.builder()
			.ticketId(ticketId)
			.userId(userId)
			.status(TrackingStatus.RESOLVED)
			.resolvedHelpType(ResolvedHelpType.NATURAL_OVER_TIME)
			.resolvedStateType(ResolvedStateType.MOSTLY_OK_SOMETIMES)
			.build();
		ReflectionTestUtils.setField(second, "createdAt", LocalDateTime.of(2026, 3, 2, 8, 30));

		when(trackingRepository.findByTicketIdOrderByCreatedAtDesc(ticketId))
			.thenReturn(List.of(first, second));

		// when
		List<TrackingHistoryItemResponse> result = trackingService.getHistory(userId, ticketId);

		// then
		assertThat(result).hasSize(2);
		assertThat(result.get(0).dDay()).isEqualTo("D+7");
		assertThat(result.get(1).dDay()).isEqualTo("D+1");
		assertThat(result.get(1).note()).isNull();
	}
}
