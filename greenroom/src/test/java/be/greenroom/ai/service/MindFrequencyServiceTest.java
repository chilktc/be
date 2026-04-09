package be.greenroom.ai.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
import be.greenroom.ticket.service.AiSessionRedisService;

@ExtendWith(MockitoExtension.class)
class MindFrequencyServiceTest {

	@Mock
	private MindFrequencyRepository mindFrequencyRepository;
	@Mock
	private AiSessionRedisService aiSessionRedisService;

	@InjectMocks
	private MindFrequencyService mindFrequencyService;

	@Test
	@DisplayName("마인드 빈도 저장 시 세션 완료 여부를 Redis에 저장한다")
	void 마인드빈도_저장시_세션완료여부_저장() {
		// given
		CreateMindFrequencyRequest request = new CreateMindFrequencyRequest(
			"session-123",
			List.of("one", "two"),
			"description"
		);
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
		CreateMindFrequencyRequest request = new CreateMindFrequencyRequest(
			"session-123",
			List.of("new", "keywords"),
			"updated"
		);
		MindFrequency existing = MindFrequency.create("session-123", List.of("old"), "before");
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
		MindFrequency frequency = MindFrequency.create("session-123", List.of("one", "two"), "new");
		ReflectionTestUtils.setField(frequency, "createdAt", LocalDateTime.of(2026, 4, 7, 10, 0));
		when(mindFrequencyRepository.findBySessionId("session-123"))
			.thenReturn(Optional.of(frequency));

		// when
		MindFrequencyResponse response = mindFrequencyService.getBySessionId("session-123");

		// then
		org.assertj.core.api.Assertions.assertThat(response.keywords()).containsExactly("one", "two");
		org.assertj.core.api.Assertions.assertThat(response.description()).isEqualTo("new");
	}

	@Test
	@DisplayName("마인드 빈도 조회 시 없으면 예외를 던진다")
	void 마인드빈도_조회_없음() {
		// given
		when(mindFrequencyRepository.findBySessionId("missing-session"))
			.thenReturn(Optional.empty());

		// when
		org.assertj.core.api.ThrowableAssert.ThrowingCallable action =
			() -> mindFrequencyService.getBySessionId("missing-session");

		// then
		org.assertj.core.api.Assertions.assertThatThrownBy(action)
			.isInstanceOf(CustomException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.DOES_NOT_EXIST_MIND_FREQUENCY);
	}
}
