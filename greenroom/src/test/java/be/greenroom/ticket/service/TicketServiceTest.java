package be.greenroom.ticket.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import be.common.api.CustomException;
import be.greenroom.notification.event.GreenroomTicketCreatedEvent;
import be.greenroom.notification.event.GreenroomTicketResolvedEvent;
import be.greenroom.notification.service.GreenroomNotificationEventPublisher;
import be.greenroom.ticket.domain.Ticket;
import be.greenroom.ticket.dto.request.CreateTicketRequest;
import be.greenroom.ticket.repository.TicketRepository;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

	@Mock
	private TicketRepository ticketRepository;
	@Mock
	private GreenroomNotificationEventPublisher eventPublisher;

	@InjectMocks
	private TicketService ticketService;

	@Test
	@DisplayName("티켓 생성 시 TICKET_CREATED 이벤트를 발행한다")
	void 티켓생성_이벤트발행() {
		// given
		UUID userId = UUID.randomUUID();
		when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> {
			Ticket ticket = invocation.getArgument(0);
			ReflectionTestUtils.setField(ticket, "createdAt", LocalDateTime.of(2026, 3, 1, 10, 0));
			return ticket;
		});

		// when
		ticketService.create(userId, new CreateTicketRequest("s", "t", "a", "c"));

		// then
		ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
		verify(eventPublisher).publish(org.mockito.ArgumentMatchers.eq(userId.toString()), eventCaptor.capture());
		org.assertj.core.api.Assertions.assertThat(eventCaptor.getValue()).isInstanceOf(GreenroomTicketCreatedEvent.class);
	}

	@Test
	@DisplayName("티켓 해결 시 TICKET_RESOLVED 이벤트를 발행한다")
	void 티켓해결_이벤트발행() {
		// given
		UUID userId = UUID.randomUUID();
		UUID ticketId = UUID.randomUUID();
		Ticket ticket = Ticket.create(userId, "n", "s", "t", "a", "c");
		ReflectionTestUtils.setField(ticket, "id", ticketId);
		when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

		// when
		ticketService.resolveTicket(userId, ticketId);

		// then
		ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
		verify(eventPublisher).publish(org.mockito.ArgumentMatchers.eq(userId.toString()), eventCaptor.capture());
		org.assertj.core.api.Assertions.assertThat(eventCaptor.getValue()).isInstanceOf(GreenroomTicketResolvedEvent.class);
	}

	@Test
	@DisplayName("타인 티켓 해결 시 예외가 발생한다")
	void 타인티켓_해결예외() {
		// given
		UUID owner = UUID.randomUUID();
		UUID other = UUID.randomUUID();
		UUID ticketId = UUID.randomUUID();
		Ticket ticket = Ticket.create(owner, "n", "s", "t", "a", "c");
		ReflectionTestUtils.setField(ticket, "id", ticketId);
		when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

		// when then
		assertThatThrownBy(() -> ticketService.resolveTicket(other, ticketId))
			.isInstanceOf(CustomException.class);
	}
}
