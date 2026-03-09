package be.notification.scheduler;

import java.time.Instant;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import be.notification.repository.GreenroomNotificationTargetRepository;
import be.notification.service.GreenroomNotificationDispatchService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GreenroomNotificationScheduler {

	private final GreenroomNotificationTargetRepository targetRepository;
	private final GreenroomNotificationDispatchService dispatchService;

	@Transactional
	@Scheduled(cron = "0 30 8 * * *", zone = "Asia/Seoul")
	public void run() {
		Instant now = Instant.now();
		targetRepository.findByResolvedFalseAndEnabledTrueAndNextSendAtLessThanEqual(now).forEach(target -> {
			boolean success = dispatchService.sendEmail(target.getUserId(), target.getTicketId(), target.getNextSequence());
			if (success) {
				target.advanceAfterSuccess();
				targetRepository.save(target);
			}
		});
	}
}
