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

@Service
@RequiredArgsConstructor
public class MindFrequencyService {

	private final MindFrequencyRepository mindFrequencyRepository;
	private final AiSessionRedisService aiSessionRedisService;

	@Transactional
	public void create(CreateMindFrequencyRequest request) {
		Optional<MindFrequency> existing = mindFrequencyRepository.findBySessionId(request.sessionId());
		if (existing.isPresent()) {
			existing.get().update(request.keywords(), request.description());
		} else {
			mindFrequencyRepository.save(
				MindFrequency.create(request.sessionId(), request.keywords(), request.description())
			);
		}
		aiSessionRedisService.saveCompleted(request.sessionId(), Duration.ofHours(1));
	}

	@Transactional(readOnly = true)
	public MindFrequencyResponse getBySessionId(String sessionId) {
		return mindFrequencyRepository.findBySessionId(sessionId)
			.map(MindFrequencyResponse::from)
			.orElseThrow(() -> new CustomException(ErrorCode.DOES_NOT_EXIST_MIND_FREQUENCY));
	}
}
