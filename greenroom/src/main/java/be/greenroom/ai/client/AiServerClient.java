package be.greenroom.ai.client;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import be.greenroom.ai.dto.request.AiStorySelectionRequest;
import be.greenroom.ai.dto.request.PodcastEpisodeRequest;
import be.greenroom.ai.dto.request.SessionCloseRequest;
import be.greenroom.ai.dto.request.SessionCreateRequest;
import be.greenroom.ai.dto.response.PodcastEpisodeResponse;
import be.greenroom.ai.dto.response.SessionCloseResponse;
import be.greenroom.ai.dto.response.SessionCreateResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class AiServerClient {

	private final WebClient aiWebClient;

	public SessionCreateResponse createSession(SessionCreateRequest request) {
		log.info("[AI_CLIENT] POST /api/sessions request={}", request);
		SessionCreateResponse response = aiWebClient.post()
			.uri("/api/sessions")
			.bodyValue(request)
			.retrieve()
			.bodyToMono(SessionCreateResponse.class)
			.block();
		log.info("[AI_CLIENT] POST /api/sessions response={}", response);
		return response;
	}

	public PodcastEpisodeResponse createPodcastEpisode(PodcastEpisodeRequest request) {
		log.info("[AI_CLIENT] POST /api/podcasts/episodes request={}", request);
		PodcastEpisodeResponse response = aiWebClient.post()
			.uri("/api/podcasts/episodes")
			.bodyValue(request)
			.retrieve()
			.bodyToMono(PodcastEpisodeResponse.class)
			.block();
		log.info("[AI_CLIENT] POST /api/podcasts/episodes response={}", response);
		return response;
	}

	public void selectStory(AiStorySelectionRequest request) {
		log.info("[AI_CLIENT] POST /api/stories/select request={}", request);
		aiWebClient.post()
			.uri("/api/stories/select")
			.bodyValue(request)
			.retrieve()
			.toBodilessEntity()
			.block();
		log.info("[AI_CLIENT] POST /api/stories/select completed");
	}

	public SessionCloseResponse closeSession(String sessionId, SessionCloseRequest request) {
		log.info("[AI_CLIENT] POST /api/sessions/{}/close request={}", sessionId, request);
		SessionCloseResponse response = aiWebClient.post()
			.uri("/api/sessions/{sessionId}/close", sessionId)
			.bodyValue(request)
			.retrieve()
			.bodyToMono(SessionCloseResponse.class)
			.block();
		log.info("[AI_CLIENT] POST /api/sessions/{}/close response={}", sessionId, response);
		return response;
	}
}
