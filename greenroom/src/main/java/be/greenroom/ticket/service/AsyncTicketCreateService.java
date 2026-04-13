package be.greenroom.ticket.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import be.common.api.ErrorCode;
import be.common.utils.Preconditions;
import be.greenroom.ai.client.AiServerClient;
import be.greenroom.ai.dto.request.PodcastEpisodeRequest;
import be.greenroom.ai.dto.response.PodcastEpisodeResponse;
import be.greenroom.notification.event.GreenroomNotificationEventType;
import be.greenroom.notification.event.GreenroomTicketCreatedEvent;
import be.greenroom.notification.service.GreenroomNotificationEventPublisher;
import be.greenroom.ticket.domain.Ticket;
import be.greenroom.ticket.dto.request.CreateTicketRequest;
import be.greenroom.ticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AsyncTicketCreateService {

	private final AiServerClient aiServerClient;
	private final TicketRepository ticketRepository;
	private final GreenroomNotificationEventPublisher eventPublisher;

	@Async
	@Transactional
	public void createPodcastAndSaveTicket(UUID userId, String sessionId, CreateTicketRequest request) {
		PodcastEpisodeResponse response = aiServerClient.createPodcastEpisode(
			new PodcastEpisodeRequest(
				userId,
				sessionId,
				request.situation(),
				request.thought(),
				request.action(),
				request.colleagueReaction(),
				null
			)
		);
		Preconditions.validate(response != null, ErrorCode.INTERNAL_SERVER_ERROR);

		Ticket ticket = Ticket.createWithSession(
			userId,
			sessionId,
			request.situation(),
			request.thought(),
			request.action(),
			request.colleagueReaction()
		);

		Ticket saved = ticketRepository.save(ticket);
		GreenroomTicketCreatedEvent event = new GreenroomTicketCreatedEvent(
			UUID.randomUUID(),
			GreenroomNotificationEventType.GREENROOM_TICKET_CREATED.name(),
			LocalDateTime.now(),
			saved.getId(),
			saved.getUserId(),
			saved.getCreatedAt()
		);
		eventPublisher.publish(saved.getUserId().toString(), event);
	}
}
