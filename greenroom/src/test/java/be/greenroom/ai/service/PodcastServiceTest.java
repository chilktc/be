package be.greenroom.ai.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import be.greenroom.ai.domain.Podcast;
import be.greenroom.ai.dto.request.PodcastEpisodeIngestRequest;
import be.greenroom.ai.repository.PodcastRepository;
import be.greenroom.ticket.domain.Ticket;
import be.greenroom.ticket.repository.TicketRepository;

@ExtendWith(MockitoExtension.class)
class PodcastServiceTest {

	@Mock
	private PodcastRepository podcastRepository;
	@Mock
	private TicketRepository ticketRepository;

	@InjectMocks
	private PodcastService podcastService;

	@Test
	@DisplayName("팟캐스트 ingest 시 sessionId로 티켓을 찾아 제목을 이름으로 저장하고 podcast를 생성한다")
	void 팟캐스트_인제스트_티켓이름저장_팟캐스트생성() {
		UUID userId = UUID.randomUUID();
		Ticket ticket = Ticket.createWithSession(
			userId,
			"session-123",
			"situation",
			"thought",
			"action",
			"reaction"
		);
		PodcastEpisodeIngestRequest request = new PodcastEpisodeIngestRequest(
			"session-123",
			"episode title",
			"https://image",
			"podcast text"
		);

		when(ticketRepository.findBySessionId("session-123")).thenReturn(Optional.of(ticket));

		podcastService.create(request);

		org.assertj.core.api.Assertions.assertThat(ticket.getName()).isEqualTo("episode title");

		ArgumentCaptor<Podcast> podcastCaptor = ArgumentCaptor.forClass(Podcast.class);
		verify(podcastRepository).save(podcastCaptor.capture());
		org.assertj.core.api.Assertions.assertThat(podcastCaptor.getValue().getUserId()).isEqualTo(userId);
		org.assertj.core.api.Assertions.assertThat(podcastCaptor.getValue().getSessionId()).isEqualTo("session-123");
	}
}
