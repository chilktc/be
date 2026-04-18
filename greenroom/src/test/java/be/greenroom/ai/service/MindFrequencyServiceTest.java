package be.greenroom.ai.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import be.common.api.CustomException;
import be.common.api.ErrorCode;
import be.greenroom.ai.domain.MindFrequency;
import be.greenroom.ai.dto.request.CreateMindFrequencyRequest;
import be.greenroom.ai.dto.response.MindFrequencyResponse;
import be.greenroom.ai.repository.MindFrequencyRepository;
import be.greenroom.ai.repository.PodcastRepository;
import be.greenroom.ticket.domain.Ticket;
import be.greenroom.ticket.repository.TicketRepository;
import be.greenroom.ticket.service.AiSessionRedisService;
import be.greenroom.tracking.repository.TrackingRepository;

@ExtendWith(MockitoExtension.class)
class MindFrequencyServiceTest {

	@Mock
	private MindFrequencyRepository mindFrequencyRepository;
	@Mock
	private AiSessionRedisService aiSessionRedisService;
	@Mock
	private PodcastRepository podcastRepository;
	@Mock
	private TicketRepository ticketRepository;
	@Mock
	private TrackingRepository trackingRepository;

	@InjectMocks
	private MindFrequencyService mindFrequencyService;

	@Test
	@DisplayName("마인드 빈도 저장 시 세션 완료 여부를 Redis에 저장한다")
	void 마인드빈도_저장시_세션완료여부_저장() {
		// given
		UUID ticketId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();
		Ticket ticket = Ticket.createWithSession(userId, "session-123", "situation", "thought", "action", "reaction");
		ReflectionTestUtils.setField(ticket, "id", ticketId);
		CreateMindFrequencyRequest request = new CreateMindFrequencyRequest(
			"session-123",
			List.of("one", "two"),
			"description"
		);
		when(ticketRepository.findBySessionId("session-123")).thenReturn(Optional.of(ticket));
		when(mindFrequencyRepository.save(any(MindFrequency.class))).thenAnswer(invocation -> {
			MindFrequency saved = invocation.getArgument(0);
			ReflectionTestUtils.setField(saved, "createdAt", LocalDateTime.of(2026, 4, 7, 10, 0));
			return saved;
		});
		when(mindFrequencyRepository.findBySessionId("session-123")).thenReturn(Optional.empty());

		// when
		mindFrequencyService.create(request);

		// then
		verify(aiSessionRedisService).saveCompleted(eq("session-123"), eq(Duration.ofHours(1)));
	}

	@Test
	@DisplayName("같은 세션으로 저장하면 기존 값을 갱신한다")
	void 마인드빈도_갱신() {
		// given
		UUID ticketId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();
		Ticket ticket = Ticket.createWithSession(userId, "session-123", "situation", "thought", "action", "reaction");
		ReflectionTestUtils.setField(ticket, "id", ticketId);
		CreateMindFrequencyRequest request = new CreateMindFrequencyRequest(
			"session-123",
			List.of("new", "keywords"),
			"updated"
		);
		MindFrequency existing = MindFrequency.create(ticketId, "session-123", List.of("old"), "before");
		when(ticketRepository.findBySessionId("session-123")).thenReturn(Optional.of(ticket));
		when(mindFrequencyRepository.findBySessionId("session-123")).thenReturn(Optional.of(existing));

		// when
		mindFrequencyService.create(request);

		// then
		org.assertj.core.api.Assertions.assertThat(existing.getKeywordsAsList()).containsExactly("new", "keywords");
		org.assertj.core.api.Assertions.assertThat(existing.getDescription()).isEqualTo("updated");
		verify(aiSessionRedisService).saveCompleted(eq("session-123"), eq(Duration.ofHours(1)));
	}

	@Test
	@DisplayName("마인드 빈도 조회 시 단건을 반환한다")
	void 마인드빈도_조회() {
		// given
		UUID userId = UUID.randomUUID();
		Ticket ticket = Ticket.createWithSession(userId, "session-123", "situation", "thought", "action", "reaction");
		MindFrequency frequency = MindFrequency.create(UUID.randomUUID(), "session-123", List.of("one", "two"), "new");
		ReflectionTestUtils.setField(frequency, "createdAt", LocalDateTime.of(2026, 4, 7, 10, 0));
		when(ticketRepository.findBySessionId("session-123")).thenReturn(Optional.of(ticket));
		when(mindFrequencyRepository.findBySessionId("session-123"))
			.thenReturn(Optional.of(frequency));

		// when
		MindFrequencyResponse response = mindFrequencyService.getBySessionId(userId, "session-123");

		// then
		org.assertj.core.api.Assertions.assertThat(response.keywords()).containsExactly("one", "two");
		org.assertj.core.api.Assertions.assertThat(response.description()).isEqualTo("new");
	}

	@Test
	@DisplayName("마인드 빈도 조회 시 없으면 예외를 던진다")
	void 마인드빈도_조회_없음() {
		// given
		UUID userId = UUID.randomUUID();
		Ticket ticket = Ticket.createWithSession(userId, "missing-session", "situation", "thought", "action", "reaction");
		when(ticketRepository.findBySessionId("missing-session")).thenReturn(Optional.of(ticket));
		when(mindFrequencyRepository.findBySessionId("missing-session"))
			.thenReturn(Optional.empty());

		// when
		org.assertj.core.api.ThrowableAssert.ThrowingCallable action =
			() -> mindFrequencyService.getBySessionId(userId, "missing-session");

		// then
		org.assertj.core.api.Assertions.assertThatThrownBy(action)
			.isInstanceOf(CustomException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.DOES_NOT_EXIST_MIND_FREQUENCY);
	}

	@Test
	@DisplayName("다른 유저의 세션으로 마인드 빈도를 조회하면 접근 거부를 던진다")
	void 다른유저_세션_마인드빈도_조회_거부() {
		// given
		UUID ownerId = UUID.randomUUID();
		UUID otherUserId = UUID.randomUUID();
		Ticket ticket = Ticket.createWithSession(ownerId, "session-123", "situation", "thought", "action", "reaction");
		when(ticketRepository.findBySessionId("session-123")).thenReturn(Optional.of(ticket));

		// when
		org.assertj.core.api.ThrowableAssert.ThrowingCallable action =
			() -> mindFrequencyService.getBySessionId(otherUserId, "session-123");

		// then
		org.assertj.core.api.Assertions.assertThatThrownBy(action)
			.isInstanceOf(CustomException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.NO_TICKET_ACCESS);
	}
}
