package be.notification.support;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import be.notification.domain.GreenroomNotificationTarget;
import be.notification.domain.NotificationChannel;
import be.notification.repository.GreenroomNotificationHistoryRepository;
import be.notification.repository.GreenroomNotificationTargetRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GreenroomNotificationBenchmarkCleanupService {

	private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");
	private static final int CHUNK_SIZE = 1000;

	private final GreenroomNotificationTargetRepository targetRepository;
	private final GreenroomNotificationHistoryRepository historyRepository;

	@Transactional
	public void restore(List<TargetSnapshot> snapshots) {
		for (int start = 0; start < snapshots.size(); start += CHUNK_SIZE) {
			int end = Math.min(start + CHUNK_SIZE, snapshots.size());
			List<TargetSnapshot> chunk = snapshots.subList(start, end);

			var ids = chunk.stream().map(TargetSnapshot::ticketId).toList();
			var targets = targetRepository.findAllById(ids);
			var snapshotById = chunk.stream().collect(java.util.stream.Collectors.toMap(TargetSnapshot::ticketId, java.util.function.Function.identity()));

			targets.forEach(target -> {
				TargetSnapshot snapshot = snapshotById.get(target.getTicketId());
				target.restoreForBenchmark(snapshot.nextSequence(), snapshot.nextSendAt());
			});
			targetRepository.saveAll(targets);
			targetRepository.flush();

			List<String> idempotencyKeys = new ArrayList<>();
			String dayKey = ZonedDateTime.now(SEOUL_ZONE).toLocalDate().toString();
			chunk.forEach(snapshot -> idempotencyKeys.add(
				snapshot.userId() + ":" + snapshot.ticketId() + ":" + dayKey + ":" + snapshot.nextSequence() + ":1:" + NotificationChannel.PUSH.name()
			));
			historyRepository.deleteByIdempotencyKeyIn(idempotencyKeys);
		}
	}

	public record TargetSnapshot(
		UUID ticketId,
		UUID userId,
		int nextSequence,
		Instant nextSendAt
	) {
		public static TargetSnapshot from(GreenroomNotificationTarget target) {
			return new TargetSnapshot(
				target.getTicketId(),
				target.getUserId(),
				target.getNextSequence(),
				target.getNextSendAt()
			);
		}
	}
}
