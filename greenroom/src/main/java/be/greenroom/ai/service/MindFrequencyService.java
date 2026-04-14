package be.greenroom.ai.service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import be.common.api.CustomException;
import be.common.api.ErrorCode;
import be.greenroom.ai.domain.MindFrequency;
import be.greenroom.ai.dto.request.CreateMindFrequencyRequest;
import be.greenroom.ai.dto.response.MindFrequencyResponse;
import be.greenroom.ai.repository.MindFrequencyRepository;
import be.greenroom.ticket.domain.Ticket;
import be.greenroom.ticket.repository.TicketRepository;
import be.greenroom.ticket.service.AiSessionRedisService;
import be.greenroom.tracking.domain.TrackingStatus;
import be.greenroom.tracking.repository.TrackingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class MindFrequencyService {

	private final MindFrequencyRepository mindFrequencyRepository;
	private final AiSessionRedisService aiSessionRedisService;
	private final TicketRepository ticketRepository;
	private final TrackingRepository trackingRepository;

	@Transactional
	public void create(CreateMindFrequencyRequest request) {
		List<String> normalizedKeywords = request.keywords().stream()
			.map(String::trim)
			.filter(keyword -> !keyword.isBlank())
			.distinct()
			.toList();

		log.info(
			"[AI_INGEST] MindFrequencyService.create start sessionId={}, keywordsCount={}, normalizedKeywordsCount={}",
			request.sessionId(),
			request.keywords() != null ? request.keywords().size() : 0,
			normalizedKeywords.size()
		);
		Ticket ticket = ticketRepository.findBySessionId(request.sessionId())
			.orElseThrow(() -> new CustomException(ErrorCode.DOES_NOT_EXIST_TICKET));

		Optional<MindFrequency> existing = mindFrequencyRepository.findBySessionId(request.sessionId());
		if (existing.isPresent()) {
			log.info("[AI_INGEST] MindFrequencyService updating existing sessionId={}, ticketId={}", request.sessionId(), ticket.getId());
			existing.get().update(normalizedKeywords, request.description());
		} else {
			log.info("[AI_INGEST] MindFrequencyService creating new sessionId={}, ticketId={}", request.sessionId(), ticket.getId());
			mindFrequencyRepository.save(
				MindFrequency.create(ticket.getId(), request.sessionId(), normalizedKeywords, request.description())
			);
		}
		log.info("[AI_INGEST] MindFrequencyService saving completed flag sessionId={}", request.sessionId());
		aiSessionRedisService.saveCompleted(request.sessionId(), Duration.ofHours(1));
		log.info("[AI_INGEST] MindFrequencyService.create completed sessionId={}", request.sessionId());
	}

	@Transactional(readOnly = true)
	public MindFrequencyResponse getBySessionId(String sessionId) {
		return mindFrequencyRepository.findBySessionId(sessionId)
			.map(MindFrequencyResponse::from)
			.orElseThrow(() -> new CustomException(ErrorCode.DOES_NOT_EXIST_MIND_FREQUENCY));
	}

	@Transactional(readOnly = true)
	public MindFrequencyResponse getByTicketId(UUID ticketId) {
		if (trackingRepository.existsByTicketIdAndStatus(ticketId, TrackingStatus.RESOLVED)) {
			throw new CustomException(ErrorCode.ALREADY_RESOLVED_TICKET);
		}

		return mindFrequencyRepository.findByTicketId(ticketId)
			.map(MindFrequencyResponse::from)
			.orElseThrow(() -> new CustomException(ErrorCode.DOES_NOT_EXIST_MIND_FREQUENCY));
	}
}
