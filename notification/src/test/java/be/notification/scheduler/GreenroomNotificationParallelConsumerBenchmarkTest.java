package be.notification.scheduler;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import be.notification.domain.NotificationChannel;
import be.notification.repository.GreenroomNotificationHistoryRepository;
import be.notification.repository.GreenroomNotificationTargetRepository;
import be.notification.service.GreenroomNotificationParallelConsumerRunner;
import be.notification.support.GreenroomNotificationBenchmarkCleanupService;

@SpringBootTest(properties = {
	"notification.dispatch.parallel-consumer.enabled=true",
	"notification.dispatch.parallel-consumer.auto-startup=false"
})
@EmbeddedKafka(partitions = 1, topics = {"greenroom.notification.dispatch", "greenroom.notification.dispatch.dlq"})
@DirtiesContext
class GreenroomNotificationParallelConsumerBenchmarkTest {

	private static final String RUN_FLAG = "RUN_GREENROOM_NOTIFICATION_PARALLEL_CONSUMER_100K_BENCHMARK";
	private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

	@Autowired
	private GreenroomNotificationScheduler scheduler;

	@Autowired
	private GreenroomNotificationTargetRepository targetRepository;

	@Autowired
	private GreenroomNotificationHistoryRepository historyRepository;

	@Autowired
	private GreenroomNotificationBenchmarkCleanupService cleanupService;

	@Autowired
	private GreenroomNotificationParallelConsumerRunner parallelConsumerRunner;

	@Test
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	@DisplayName("parallel-consumer 10 concurrency로 미리 적재된 due target dispatch를 batch 처리한 뒤 수동 복구한다")
	void parallelConsumer_벤치마크() throws Exception {
		boolean shouldRun = Boolean.parseBoolean(System.getenv().getOrDefault(RUN_FLAG, "false"));
		if (!shouldRun) {
			System.out.println("[SKIP] Set " + RUN_FLAG + "=true to run parallel-consumer benchmark.");
			return;
		}

		List<GreenroomNotificationBenchmarkCleanupService.TargetSnapshot> snapshots =
			targetRepository.findByResolvedFalseAndEnabledTrueAndNextSendAtLessThanEqual(Instant.now()).stream()
				.map(GreenroomNotificationBenchmarkCleanupService.TargetSnapshot::from)
				.toList();
		Set<String> idempotencyKeys = snapshots.stream()
			.map(this::idempotencyKey)
			.collect(Collectors.toSet());

		assertThat(snapshots).isNotEmpty();

		try (var runningConsumer = parallelConsumerRunner.start()) {
			Thread.sleep(200);
			long startedAt = System.nanoTime();
			scheduler.run();
			waitUntilProcessed(runningConsumer, idempotencyKeys, Duration.ofMinutes(10));
			long elapsedNanos = System.nanoTime() - startedAt;
			double elapsedSeconds = Duration.ofNanos(elapsedNanos).toMillis() / 1000.0;

			System.out.printf(
				"[GREENROOM PARALLEL-CONSUMER BENCHMARK] eligible=%d, elapsedSec=%.3f, msgsPerSecond=%.2f%n",
				snapshots.size(),
				elapsedSeconds,
				elapsedSeconds == 0.0 ? 0.0 : snapshots.size() / elapsedSeconds
			);
		} finally {
			cleanupService.restore(snapshots);
		}
	}

	private void waitUntilProcessed(
		GreenroomNotificationParallelConsumerRunner.RunningConsumer runningConsumer,
		Set<String> idempotencyKeys,
		Duration timeout
	) throws InterruptedException {
		long deadline = System.currentTimeMillis() + timeout.toMillis();
		while (System.currentTimeMillis() < deadline) {
			runningConsumer.rethrowIfFailed();
			long processed = historyRepository.countByIdempotencyKeyIn(idempotencyKeys);
			long remaining = idempotencyKeys.size() - processed;
			if (remaining == 0L) {
				return;
			}
			System.out.printf(
				"[GREENROOM PARALLEL-CONSUMER BENCHMARK PROGRESS] processed=%d/%d, remaining=%d%n",
				processed,
				idempotencyKeys.size(),
				remaining
			);
			Thread.sleep(100);
		}
		throw new AssertionError("Parallel-consumer benchmark was not completed within " + timeout);
	}

	private String idempotencyKey(GreenroomNotificationBenchmarkCleanupService.TargetSnapshot snapshot) {
		String dayKey = ZonedDateTime.now(SEOUL_ZONE).toLocalDate().toString();
		return snapshot.userId() + ":" + snapshot.ticketId() + ":" + dayKey + ":" + snapshot.nextSequence() + ":1:" + NotificationChannel.PUSH.name();
	}
}
