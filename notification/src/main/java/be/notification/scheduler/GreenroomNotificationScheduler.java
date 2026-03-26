package be.notification.scheduler;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.kafka.core.KafkaTemplate;

import be.notification.event.GreenroomNotificationDispatchRequestEvent;
import be.notification.repository.GreenroomNotificationTargetRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GreenroomNotificationScheduler {

	private static final String TOPIC = "greenroom.notification.dispatch";

	private final GreenroomNotificationTargetRepository targetRepository;
	private final KafkaTemplate<String, String> kafkaTemplate;
	private final ObjectMapper objectMapper;

	@Transactional(readOnly = true)
	@Scheduled(cron = "0 30 8 * * *", zone = "Asia/Seoul")
	public void run() {
		Instant now = Instant.now();
		CompletableFuture<?>[] futures = targetRepository.findByResolvedFalseAndEnabledTrueAndNextSendAtLessThanEqual(now).stream()
			.map(target -> publishDispatchRequest(target.getTicketId(), target.getUserId(), target.getNextSequence()))
			.toArray(CompletableFuture[]::new);
		CompletableFuture.allOf(futures).join();
	}

	private CompletableFuture<?> publishDispatchRequest(java.util.UUID ticketId, java.util.UUID userId, int sequence) {
		GreenroomNotificationDispatchRequestEvent event = new GreenroomNotificationDispatchRequestEvent(
			ticketId,
			userId,
			sequence
		);
		try {
			return kafkaTemplate.send(TOPIC, ticketId.toString(), objectMapper.writeValueAsString(event));
		} catch (JsonProcessingException exception) {
			throw new IllegalStateException("Failed to publish dispatch request", exception);
		}
	}
}
