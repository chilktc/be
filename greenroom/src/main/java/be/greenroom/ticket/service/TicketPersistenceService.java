package be.greenroom.ticket.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import be.greenroom.ticket.domain.Ticket;
import be.greenroom.ticket.dto.request.CreateTicketRequest;
import be.greenroom.ticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class TicketPersistenceService {

	private final TicketRepository ticketRepository;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Ticket saveOrUpdateAiTicket(UUID userId, String sessionId, CreateTicketRequest request) {
		Ticket ticket = ticketRepository.findBySessionId(sessionId)
			.map(existing -> {
				log.info("[AI_TICKET][TX] found existing ticket ticketId={}, sessionId={}, updating fields", existing.getId(), sessionId);
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
				log.info("[AI_TICKET][TX] creating new ticket entity userId={}, sessionId={}", userId, sessionId);
				return Ticket.createWithSession(
					userId,
					sessionId,
					request.situation(),
					request.thought(),
					request.action(),
					request.colleagueReaction()
				);
			});

		log.info("[AI_TICKET][TX] saving and flushing ticket userId={}, sessionId={}", userId, sessionId);
		Ticket saved = ticketRepository.saveAndFlush(ticket);
		log.info("[AI_TICKET][TX] committed ticket persistence ticketId={}, userId={}, sessionId={}", saved.getId(), saved.getUserId(), sessionId);
		return saved;
	}
}
