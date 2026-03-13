package be.greenroom.ai.client;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import be.greenroom.ai.dto.request.PodcastEpisodeRequest;
import be.greenroom.ai.dto.request.SessionCloseRequest;
import be.greenroom.ai.dto.request.SessionCreateRequest;
import be.greenroom.ai.dto.response.PodcastEpisodeResponse;
import be.greenroom.ai.dto.response.SessionCloseResponse;
import be.greenroom.ai.dto.response.SessionCreateResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AiServerClient {

	private final WebClient aiWebClient;

	public SessionCreateResponse createSession(SessionCreateRequest request) {
		return aiWebClient.post()
			.uri("/api/v1/sessions")
			.bodyValue(request)
			.retrieve()
			.bodyToMono(SessionCreateResponse.class)
			.block();
	}

	public PodcastEpisodeResponse createPodcastEpisode(PodcastEpisodeRequest request) {
		return aiWebClient.post()
			.uri("/api/v1/podcasts/episodes")
			.bodyValue(request)
			.retrieve()
			.bodyToMono(PodcastEpisodeResponse.class)
			.block();
	}

	public SessionCloseResponse closeSession(String sessionId, SessionCloseRequest request) {
		return aiWebClient.post()
			.uri("/api/v1/sessions/{sessionId}/close", sessionId)
			.bodyValue(request)
			.retrieve()
			.bodyToMono(SessionCloseResponse.class)
			.block();
	}
}
