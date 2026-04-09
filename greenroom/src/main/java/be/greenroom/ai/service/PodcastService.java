package be.greenroom.ai.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import be.greenroom.ai.domain.Podcast;
import be.greenroom.ai.dto.request.PodcastEpisodeIngestRequest;
import be.greenroom.ai.repository.PodcastRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PodcastService {

	private final PodcastRepository podcastRepository;

	@Transactional
	public void create(PodcastEpisodeIngestRequest request) {
		podcastRepository.save(
			Podcast.create(
				request.userId(),
				request.sessionId(),
				request.imageUrl(),
				request.text()
			)
		);
	}
}
