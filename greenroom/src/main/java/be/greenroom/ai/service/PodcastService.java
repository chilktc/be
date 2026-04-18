package be.greenroom.ai.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import be.common.api.CustomException;
import be.common.api.ErrorCode;
import be.common.utils.Preconditions;
import be.greenroom.ai.domain.Podcast;
import be.greenroom.ai.dto.request.PodcastEpisodeIngestRequest;
import be.greenroom.ai.dto.response.PodcastResponse;
import be.greenroom.ai.repository.PodcastRepository;
import be.greenroom.ticket.domain.Ticket;
import be.greenroom.ticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PodcastService {

	private final PodcastRepository podcastRepository;
	private final TicketRepository ticketRepository;

	@Transactional
	public void create(PodcastEpisodeIngestRequest request) {
		log.info("[AI_INGEST] PodcastService.create start sessionId={}, title={}", request.sessionId(), request.title());
		Ticket ticket = ticketRepository.findBySessionId(request.sessionId())
			.orElseThrow(() -> new CustomException(ErrorCode.DOES_NOT_EXIST_TICKET));
		log.info("[AI_INGEST] PodcastService found ticket ticketId={}, userId={}", ticket.getId(), ticket.getUserId());

		ticket.changeName(request.title());
		log.info("[AI_INGEST] PodcastService updated ticket name ticketId={}, title={}", ticket.getId(), request.title());

		podcastRepository.save(
			Podcast.create(
				ticket.getUserId(),
				request.sessionId(),
				request.imageUrl(),
				request.text()
			)
		);
		log.info("[AI_INGEST] PodcastService saved podcast sessionId={}, userId={}", request.sessionId(), ticket.getUserId());
	}

	@Transactional(readOnly = true)
	public PodcastResponse getBySessionId(UUID userId, String sessionId) {
		Ticket ticket = ticketRepository.findBySessionId(sessionId)
			.orElseThrow(() -> new CustomException(ErrorCode.DOES_NOT_EXIST_TICKET));
		validateOwner(ticket, userId);

		return podcastRepository.findBySessionId(sessionId)
			.map(PodcastResponse::from)
			.orElseThrow(() -> new CustomException(ErrorCode.DOES_NOT_EXIST_PODCAST));
	}

	@Transactional(readOnly = true)
	public PodcastResponse getByTicketId(UUID userId, UUID ticketId) {
		Ticket ticket = ticketRepository.findById(ticketId)
			.orElseThrow(() -> new CustomException(ErrorCode.DOES_NOT_EXIST_TICKET));
		validateOwner(ticket, userId);
		return getBySessionId(userId, ticket.getSessionId());
	}

	private void validateOwner(Ticket ticket, UUID userId) {
		Preconditions.validate(userId.equals(ticket.getUserId()), ErrorCode.NO_TICKET_ACCESS);
	}
}
