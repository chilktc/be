package be.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import be.notification.domain.GreenroomNotificationHistory;

public interface GreenroomNotificationHistoryRepository extends JpaRepository<GreenroomNotificationHistory, Long> {

	boolean existsByIdempotencyKey(String idempotencyKey);
}
