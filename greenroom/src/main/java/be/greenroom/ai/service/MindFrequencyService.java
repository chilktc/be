package be.greenroom.ai.service;

import java.time.Duration;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import be.common.api.CustomException;
import be.common.api.ErrorCode;
import be.greenroom.ai.domain.MindFrequency;
import be.greenroom.ai.dto.request.CreateMindFrequencyRequest;
import be.greenroom.ai.dto.response.MindFrequencyResponse;
import be.greenroom.ai.repository.MindFrequencyRepository;
import be.greenroom.ticket.service.AiSessionRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class MindFrequencyService {

	private final MindFrequencyRepository mindFrequencyRepository;
	private final AiSessionRedisService aiSessionRedisService;

	@Transactional
	public void create(CreateMindFrequencyRequest request) {
		log.info(
			"[AI_INGEST] MindFrequencyService.create start sessionId={}, keywordsCount={}",
			request.sessionId(),
			request.keywords() != null ? request.keywords().size() : 0
		);
		Optional<MindFrequency> existing = mindFrequencyRepository.findBySessionId(request.sessionId());
		if (existing.isPresent()) {
			log.info("[AI_INGEST] MindFrequencyService updating existing sessionId={}", request.sessionId());
			existing.get().update(request.keywords(), request.description());
		} else {
			log.info("[AI_INGEST] MindFrequencyService creating new sessionId={}", request.sessionId());
			mindFrequencyRepository.save(
				MindFrequency.create(request.sessionId(), request.keywords(), request.description())
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
}
