package be.notification.service;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import be.notification.domain.GreenroomNotificationHistory;
import be.notification.domain.GreenroomNotificationSchedule;
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

	private final GreenroomTemplateRegistry templateRegistry;
	private final GreenroomNotificationHistoryRepository historyRepository;

	@Transactional
	public void sendEmail(GreenroomNotificationSchedule schedule) {
		GreenroomTemplateCode templateCode = GreenroomTemplateCode.fromSequence(schedule.getNextSequence());
		GreenroomTemplate template = templateRegistry.get(templateCode);
		// TODO : 리다이렉트 주소 수정
		String deepLink = "myapp://tracking/greenroom/" + schedule.getTicketId();
		String webFallback = "https://app.example.com/tracking/greenroom/" + schedule.getTicketId();

		sendEmail(schedule, templateCode, template, deepLink, webFallback);
	}

	@Transactional
	public boolean markEmailFailedIfUnsent(GreenroomNotificationSchedule schedule, String errorCode) {
		GreenroomTemplateCode templateCode = GreenroomTemplateCode.fromSequence(schedule.getNextSequence());
		String idempotencyKey = idempotencyKey(schedule, NotificationChannel.EMAIL);
		if (historyRepository.existsByIdempotencyKey(idempotencyKey)) {
			return false;
		}

		Instant failedAt = Instant.now();
		historyRepository.save(
			GreenroomNotificationHistory.fail(
				schedule.getId(),
				schedule.getNextSequence(),
				NotificationChannel.EMAIL,
				templateCode,
				idempotencyKey,
				failedAt,
				errorCode
			)
		);
		log.warn(
			"[EMAIL][FAIL] userId={}, ticketId={}, seq={}, errorCode={}",
			schedule.getUserId(),
			schedule.getTicketId(),
			schedule.getNextSequence(),
			errorCode
		);
		return true;
	}

	private void sendEmail(
		GreenroomNotificationSchedule schedule,
		GreenroomTemplateCode templateCode,
		GreenroomTemplate template,
		String deepLink,
		String webFallback
	) {
		String idempotencyKey = idempotencyKey(schedule, NotificationChannel.EMAIL);
		if (historyRepository.existsByIdempotencyKey(idempotencyKey)) {
			return;
		}

		Instant sentAt = Instant.now();
		// TODO : 실제 이메일을 전송하도록 수정
		log.info(
			"[EMAIL] userId={}, ticketId={}, seq={}, subject={}, body={}, cta={}, deepLink={}, webFallback={}",
			schedule.getUserId(),
			schedule.getTicketId(),
			schedule.getNextSequence(),
			template.subject(),
			template.body(),
			template.ctaText(),
			deepLink,
			webFallback
		);
		historyRepository.save(
			GreenroomNotificationHistory.success(
				schedule.getId(),
				schedule.getNextSequence(),
				NotificationChannel.EMAIL,
				templateCode,
				idempotencyKey,
				sentAt
			)
		);
	}

	private String idempotencyKey(GreenroomNotificationSchedule schedule, NotificationChannel channel) {
		return schedule.getId() + ":" + schedule.getNextSequence() + ":" + channel.name();
	}
}
