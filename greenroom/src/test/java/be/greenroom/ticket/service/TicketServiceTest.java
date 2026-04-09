package be.greenroom.ticket.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import be.greenroom.ai.client.AiServerClient;
import be.greenroom.ai.dto.response.SessionCreateResponse;
import be.greenroom.notification.event.GreenroomTicketCreatedEvent;
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
	@Mock
	private AiServerClient aiServerClient;
	@Mock
	private AiSessionRedisService aiSessionRedisService;
	@Mock
	private AsyncTicketCreateService asyncTicketCreateService;

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
	@DisplayName("AI 연동 티켓 생성 시 세션을 저장하고 비동기 생성을 요청한다")
	void AI연동_티켓생성_세션저장_비동기요청() {
		// given
		UUID userId = UUID.randomUUID();
		CreateTicketRequest request = new CreateTicketRequest("situation", "thought", "action", "reaction");
		when(aiServerClient.createSession(any()))
			.thenReturn(new SessionCreateResponse("session-123", "podcast", "2026-04-07T10:00:00"));

		// when
		String sessionId = ticketService.createWithAi(userId, request);

		// then
		org.assertj.core.api.Assertions.assertThat(sessionId).isEqualTo("session-123");
		verify(aiSessionRedisService).delete(userId);
		verify(aiSessionRedisService).save(eq(userId), eq("session-123"), eq(Duration.ofHours(1)));
		verify(asyncTicketCreateService).createPodcastAndSaveTicket(userId, "session-123", request);
	}
}
