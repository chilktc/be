package be.notification.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import be.notification.service.GreenroomNotificationScheduleService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GreenroomNotificationScheduler {

	private final GreenroomNotificationScheduleService scheduleService;

	@Scheduled(fixedDelay = 60_000)
	public void run() {
		scheduleService.sendDueSchedules();
	}
}
