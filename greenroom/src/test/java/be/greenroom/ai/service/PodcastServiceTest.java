package be.greenroom.ai.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import be.common.api.CustomException;
import be.common.api.ErrorCode;
import be.greenroom.ai.domain.Podcast;
import be.greenroom.ai.dto.request.PodcastEpisodeIngestRequest;
import be.greenroom.ai.dto.response.PodcastResponse;
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

	@Test
	@DisplayName("본인 세션으로 팟캐스트를 조회한다")
	void 팟캐스트_세션조회_성공() {
		UUID userId = UUID.randomUUID();
		Ticket ticket = Ticket.createWithSession(userId, "session-123", "situation", "thought", "action", "reaction");
		Podcast podcast = Podcast.create(userId, "session-123", "https://image", "podcast text");
		ReflectionTestUtils.setField(podcast, "createdAt", LocalDateTime.of(2026, 4, 7, 10, 0));

		when(ticketRepository.findBySessionId("session-123")).thenReturn(Optional.of(ticket));
		when(podcastRepository.findBySessionId("session-123")).thenReturn(Optional.of(podcast));

		PodcastResponse response = podcastService.getBySessionId(userId, "session-123");

		org.assertj.core.api.Assertions.assertThat(response.sessionId()).isEqualTo("session-123");
		org.assertj.core.api.Assertions.assertThat(response.text()).isEqualTo("podcast text");
	}

	@Test
	@DisplayName("다른 유저의 세션으로 팟캐스트를 조회하면 접근 거부를 던진다")
	void 다른유저_세션_팟캐스트조회_거부() {
		UUID ownerId = UUID.randomUUID();
		UUID otherUserId = UUID.randomUUID();
		Ticket ticket = Ticket.createWithSession(ownerId, "session-123", "situation", "thought", "action", "reaction");

		when(ticketRepository.findBySessionId("session-123")).thenReturn(Optional.of(ticket));

		org.assertj.core.api.Assertions.assertThatThrownBy(() -> podcastService.getBySessionId(otherUserId, "session-123"))
			.isInstanceOf(CustomException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.NO_TICKET_ACCESS);
	}
}
