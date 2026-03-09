package be.notification.service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import be.notification.domain.GreenroomNotificationHistory;
import be.notification.domain.GreenroomTemplateCode;
import be.notification.domain.NotificationChannel;
import be.notification.repository.GreenroomNotificationHistoryRepository;
import be.notification.template.GreenroomTemplate;
import be.notification.template.GreenroomTemplateRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GreenroomNotificationDispatchService {

	private static final int MAX_RETRY_ATTEMPTS = 3;
	private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

	private final GreenroomTemplateRegistry templateRegistry;
	private final GreenroomNotificationHistoryRepository historyRepository;

	@Transactional
	public boolean sendEmail(UUID userId, UUID ticketId, int sequence) {
		GreenroomTemplateCode templateCode = GreenroomTemplateCode.fromSequence(sequence);
		GreenroomTemplate template = templateRegistry.get(templateCode);
		String deepLink = "myapp://tracking/greenroom/" + ticketId;
		String webFallback = "https://app.example.com/tracking/greenroom/" + ticketId;

		for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
			try {
				sendEmail(userId, ticketId, sequence, attempt, templateCode, template, deepLink, webFallback);
				return true;
			} catch (Exception exception) {
				saveFailure(userId, ticketId, sequence, attempt, templateCode, exception.getClass().getSimpleName());
			}
		}
		return false;
	}

	private void sendEmail(
		UUID userId,
		UUID ticketId,
		int sequence,
		int attempt,
		GreenroomTemplateCode templateCode,
		GreenroomTemplate template,
		String deepLink,
		String webFallback
	) {
		String idempotencyKey = idempotencyKey(userId, ticketId, sequence, attempt, NotificationChannel.EMAIL);
		if (historyRepository.existsByIdempotencyKey(idempotencyKey)) {
			return;
		}

		Instant sentAt = Instant.now();
		// TODO : 실제 이메일 전송 로직 연동
		log.info(
			"[EMAIL] userId={}, ticketId={}, seq={}, attempt={}, subject={}, body={}, cta={}, deepLink={}, webFallback={}",
			userId,
			ticketId,
			sequence,
			attempt,
			template.subject(),
			template.body(),
			template.ctaText(),
			deepLink,
			webFallback
		);
		historyRepository.save(
			GreenroomNotificationHistory.success(
				userId,
				ticketId,
				sequence,
				attempt,
				NotificationChannel.EMAIL,
				templateCode,
				idempotencyKey,
				sentAt
			)
		);
	}

	private void saveFailure(
		UUID userId,
		UUID ticketId,
		int sequence,
		int attempt,
		GreenroomTemplateCode templateCode,
		String errorCode
	) {
		String idempotencyKey = idempotencyKey(userId, ticketId, sequence, attempt, NotificationChannel.EMAIL);
		if (historyRepository.existsByIdempotencyKey(idempotencyKey)) {
			return;
		}

		Instant failedAt = Instant.now();
		historyRepository.save(
			GreenroomNotificationHistory.fail(
				userId,
				ticketId,
				sequence,
				attempt,
				NotificationChannel.EMAIL,
				templateCode,
				idempotencyKey,
				failedAt
			)
		);
		log.warn(
			"[EMAIL][FAIL] userId={}, ticketId={}, seq={}, attempt={}, errorCode={}",
			userId,
			ticketId,
			sequence,
			attempt,
			errorCode
		);
	}

	private String idempotencyKey(UUID userId, UUID ticketId, int sequence, int attempt, NotificationChannel channel) {
		String dayKey = ZonedDateTime.now(SEOUL_ZONE).toLocalDate().toString();
		return userId + ":" + ticketId + ":" + dayKey + ":" + sequence + ":" + attempt + ":" + channel.name();
	}
}
