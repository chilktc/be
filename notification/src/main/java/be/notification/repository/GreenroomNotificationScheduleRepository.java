package be.notification.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import be.notification.domain.GreenroomNotificationSchedule;
import be.notification.domain.NotificationScheduleStatus;

public interface GreenroomNotificationScheduleRepository extends JpaRepository<GreenroomNotificationSchedule, Long> {

	Optional<GreenroomNotificationSchedule> findByTicketId(UUID ticketId);

	List<GreenroomNotificationSchedule> findByStatusAndNextSendAtBetween(
		NotificationScheduleStatus status,
		Instant startInclusive,
		Instant endInclusive
	);

	List<GreenroomNotificationSchedule> findByStatusAndNextSendAtBefore(
		NotificationScheduleStatus status,
		Instant cutoffExclusive
	);
}
