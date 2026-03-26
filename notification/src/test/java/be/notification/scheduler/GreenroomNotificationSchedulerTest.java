package be.notification.scheduler;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import be.notification.domain.GreenroomNotificationTarget;
import be.notification.repository.GreenroomNotificationTargetRepository;

@ExtendWith(MockitoExtension.class)
class GreenroomNotificationSchedulerTest {

	@Mock
	private GreenroomNotificationTargetRepository targetRepository;
	@Mock
	private KafkaTemplate<String, String> kafkaTemplate;
	@Mock
	private ObjectMapper objectMapper;

	@InjectMocks
	private GreenroomNotificationScheduler scheduler;

	@Test
	@DisplayName("due target 조회 후 dispatch request 이벤트를 발행한다")
	void dueTarget조회후_dispatch이벤트발행() throws Exception {
		GreenroomNotificationTarget target = GreenroomNotificationTarget.create(
			UUID.randomUUID(),
			UUID.randomUUID(),
			LocalDateTime.of(2026, 3, 1, 10, 0),
			true
		);
		when(targetRepository.findByResolvedFalseAndEnabledTrueAndNextSendAtLessThanEqual(any(Instant.class)))
			.thenReturn(List.of(target));
		when(objectMapper.writeValueAsString(org.mockito.ArgumentMatchers.any())).thenReturn("{}");
		when(kafkaTemplate.send(anyString(), anyString(), anyString()))
			.thenReturn(CompletableFuture.completedFuture(null));

		scheduler.run();

		verify(kafkaTemplate).send(
			"greenroom.notification.dispatch",
			target.getTicketId().toString(),
			"{}"
		);
	}
}
