package be.notification.service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import be.notification.domain.GreenroomNotificationHistory;
import be.notification.domain.GreenroomNotificationTarget;
import be.notification.domain.GreenroomTemplateCode;
import be.notification.domain.NotificationChannel;
import be.notification.event.GreenroomNotificationDispatchRequestEvent;
import be.notification.repository.GreenroomNotificationHistoryRepository;
import be.notification.repository.GreenroomNotificationTargetRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GreenroomNotificationDispatchBatchService {

	private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

	private final GreenroomNotificationHistoryRepository historyRepository;
	private final GreenroomNotificationTargetRepository targetRepository;

	public GreenroomNotificationDispatchPersistenceCommand toPersistenceCommand(GreenroomNotificationDispatchRequestEvent event) {
		return new GreenroomNotificationDispatchPersistenceCommand(
			event.ticketId(),
			event.userId(),
			event.sequence(),
			idempotencyKey(event.userId(), event.ticketId(), event.sequence()),
			GreenroomTemplateCode.fromSequence(event.sequence()),
			Instant.now()
		);
	}

	@Transactional
	public void consumeBatch(List<GreenroomNotificationDispatchPersistenceCommand> commands) {
		if (commands.isEmpty()) {
			return;
		}

		List<String> idempotencyKeys = commands.stream()
			.map(GreenroomNotificationDispatchPersistenceCommand::idempotencyKey)
			.toList();

		var existingKeys = historyRepository.findByIdempotencyKeyIn(idempotencyKeys).stream()
			.map(GreenroomNotificationHistory::getIdempotencyKey)
			.collect(Collectors.toSet());

		List<GreenroomNotificationDispatchPersistenceCommand> filteredCommands = commands.stream()
			.filter(command -> !existingKeys.contains(command.idempotencyKey()))
			.toList();

		if (filteredCommands.isEmpty()) {
			return;
		}

		historyRepository.saveAll(
			filteredCommands.stream()
				.map(command -> GreenroomNotificationHistory.success(
					command.userId(),
					command.ticketId(),
					command.sequence(),
					1,
					NotificationChannel.PUSH,
					command.idempotencyKey(),
					command.sentAt()
				))
				.toList()
		);

		Map<UUID, GreenroomNotificationTarget> targetByTicketId = targetRepository.findAllById(
			filteredCommands.stream()
				.map(GreenroomNotificationDispatchPersistenceCommand::ticketId)
				.distinct()
				.toList()
		).stream().collect(Collectors.toMap(
			GreenroomNotificationTarget::getTicketId,
			Function.identity()
		));

		filteredCommands.forEach(command -> {
			GreenroomNotificationTarget target = targetByTicketId.get(command.ticketId());
			if (target != null && target.getNextSequence() == command.sequence()) {
				target.advanceAfterSuccess();
			}
		});

		historyRepository.flush();
		targetRepository.flush();
	}

	private String idempotencyKey(UUID userId, UUID ticketId, int sequence) {
		String dayKey = ZonedDateTime.now(SEOUL_ZONE).toLocalDate().toString();
		return userId + ":" + ticketId + ":" + dayKey + ":" + sequence + ":1:" + NotificationChannel.PUSH.name();
	}
}
