package be.greenroom.ai.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import be.common.api.CustomException;
import be.common.api.ErrorCode;
import be.greenroom.ai.domain.Podcast;
import be.greenroom.ai.dto.request.PodcastEpisodeIngestRequest;
import be.greenroom.ai.repository.PodcastRepository;
import be.greenroom.ticket.domain.Ticket;
import be.greenroom.ticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PodcastService {

	private final PodcastRepository podcastRepository;
	private final TicketRepository ticketRepository;

	@Transactional
	public void create(PodcastEpisodeIngestRequest request) {
		Ticket ticket = ticketRepository.findBySessionId(request.sessionId())
			.orElseThrow(() -> new CustomException(ErrorCode.DOES_NOT_EXIST_TICKET));

		ticket.changeName(request.title());

		podcastRepository.save(
			Podcast.create(
				ticket.getUserId(),
				request.sessionId(),
				request.imageUrl(),
				request.text()
			)
		);
	}
}
