package be.notification.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import be.notification.domain.GreenroomNotificationHistory;

public interface GreenroomNotificationHistoryRepository extends JpaRepository<GreenroomNotificationHistory, Long> {

	boolean existsByIdempotencyKey(String idempotencyKey);

	List<GreenroomNotificationHistory> findByScheduleIdInOrderByIdAsc(Collection<Long> scheduleIds);
}
