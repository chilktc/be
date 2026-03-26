package be.notification.service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GreenroomNotificationDispatchBatchBufferService {

	private static final int BATCH_SIZE = 200;
	private static final int STRIPE_COUNT = 4;
	private static final Duration IDLE_FLUSH_AFTER = Duration.ofSeconds(1);

	private final GreenroomNotificationDispatchBatchService batchService;

	private final List<StripeBuffer> stripes = IntStream.range(0, STRIPE_COUNT)
		.mapToObj(index -> new StripeBuffer())
		.toList();

	public void accept(GreenroomNotificationDispatchPersistenceCommand command) {
		StripeBuffer stripe = stripeFor(command);
		List<GreenroomNotificationDispatchPersistenceCommand> flushTarget = null;
		synchronized (stripe) {
			stripe.buffer.add(command);
			stripe.lastConsumedAt = Instant.now();
			if (stripe.buffer.size() >= BATCH_SIZE) {
				flushTarget = drainBuffer(stripe);
			}
		}
		if (flushTarget != null) {
			batchService.consumeBatch(flushTarget);
		}
	}

	@Scheduled(fixedDelay = 1000)
	public void flushRemainingIfIdle() {
		Instant now = Instant.now();
		for (StripeBuffer stripe : stripes) {
			List<GreenroomNotificationDispatchPersistenceCommand> flushTarget = null;
			synchronized (stripe) {
				if (stripe.buffer.isEmpty()) {
					continue;
				}
				if (stripe.lastConsumedAt.plus(IDLE_FLUSH_AFTER).isAfter(now)) {
					continue;
				}
				flushTarget = drainBuffer(stripe);
			}
			batchService.consumeBatch(flushTarget);
		}
	}

	public void flushAll() {
		for (StripeBuffer stripe : stripes) {
			List<GreenroomNotificationDispatchPersistenceCommand> flushTarget = null;
			synchronized (stripe) {
				if (stripe.buffer.isEmpty()) {
					continue;
				}
				flushTarget = drainBuffer(stripe);
			}
			batchService.consumeBatch(flushTarget);
		}
	}

	private StripeBuffer stripeFor(GreenroomNotificationDispatchPersistenceCommand command) {
		int index = Math.abs(command.ticketId().hashCode()) % STRIPE_COUNT;
		return stripes.get(index);
	}

	private List<GreenroomNotificationDispatchPersistenceCommand> drainBuffer(StripeBuffer stripe) {
		List<GreenroomNotificationDispatchPersistenceCommand> drained = new ArrayList<>(stripe.buffer);
		stripe.buffer.clear();
		return drained;
	}

	private static class StripeBuffer {
		private final List<GreenroomNotificationDispatchPersistenceCommand> buffer = new ArrayList<>();
		private Instant lastConsumedAt = Instant.EPOCH;
	}
}
