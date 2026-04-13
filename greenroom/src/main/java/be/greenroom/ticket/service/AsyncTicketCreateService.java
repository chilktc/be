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
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AsyncTicketCreateService {

	private final AiServerClient aiServerClient;
	private final TicketRepository ticketRepository;
	private final GreenroomNotificationEventPublisher eventPublisher;

	@Async
	@Transactional
	public void createPodcastAndSaveTicket(UUID userId, String sessionId, CreateTicketRequest request) {
		log.info("[AI_TICKET][ASYNC] start createPodcastAndSaveTicket userId={}, sessionId={}", userId, sessionId);
		Ticket ticket = ticketRepository.findBySessionId(sessionId)
			.map(existing -> {
				log.info("[AI_TICKET][ASYNC] found existing ticket ticketId={}, sessionId={}, updating fields", existing.getId(), sessionId);
				existing.updateForAiSession(
					userId,
					sessionId,
					request.situation(),
					request.thought(),
					request.action(),
					request.colleagueReaction()
				);
				return existing;
			})
			.orElseGet(() -> {
				log.info("[AI_TICKET][ASYNC] creating ticket entity userId={}, sessionId={}", userId, sessionId);
				return Ticket.createWithSession(
					userId,
					sessionId,
					request.situation(),
					request.thought(),
					request.action(),
					request.colleagueReaction()
				);
			});

		log.info("[AI_TICKET][ASYNC] saving and flushing ticket before AI call userId={}, sessionId={}", userId, sessionId);
		Ticket saved = ticketRepository.saveAndFlush(ticket);
		log.info("[AI_TICKET][ASYNC] ticket persisted before AI call ticketId={}, userId={}, sessionId={}", saved.getId(), saved.getUserId(), sessionId);

		log.info("[AI_TICKET][ASYNC] requesting AI podcast episode userId={}, sessionId={}", userId, sessionId);
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
		log.info("[AI_TICKET][ASYNC] AI podcast response userId={}, sessionId={}, response={}", userId, sessionId, response);
		Preconditions.validate(response != null, ErrorCode.INTERNAL_SERVER_ERROR);

		GreenroomTicketCreatedEvent event = new GreenroomTicketCreatedEvent(
			UUID.randomUUID(),
			GreenroomNotificationEventType.GREENROOM_TICKET_CREATED.name(),
			LocalDateTime.now(),
			saved.getId(),
			saved.getUserId(),
			saved.getCreatedAt()
		);
		log.info("[AI_TICKET][ASYNC] publishing notification event ticketId={}, userId={}", saved.getId(), saved.getUserId());
		eventPublisher.publish(saved.getUserId().toString(), event);
		log.info("[AI_TICKET][ASYNC] completed createPodcastAndSaveTicket ticketId={}, userId={}, sessionId={}", saved.getId(), saved.getUserId(), sessionId);
	}
}
