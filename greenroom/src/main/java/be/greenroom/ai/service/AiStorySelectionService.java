package be.greenroom.ai.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import be.greenroom.ai.client.AiServerClient;
import be.greenroom.ai.dto.request.AiStorySelectionRequest;
import be.greenroom.ai.dto.request.StorySelectionRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AiStorySelectionService {

	private final AiServerClient aiServerClient;

	@Transactional
	public void sendSelection(String sessionId, StorySelectionRequest request) {
		aiServerClient.selectStory(
			new AiStorySelectionRequest(
				sessionId,
				request.keywords(),
				request.title(),
				request.description()
			)
		);
	}
}
