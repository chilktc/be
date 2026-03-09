package be.notification.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import be.notification.domain.GreenroomNotificationTarget;

public interface GreenroomNotificationTargetRepository extends JpaRepository<GreenroomNotificationTarget, UUID> {

	List<GreenroomNotificationTarget> findByResolvedFalseAndEnabledTrueAndNextSendAtLessThanEqual(Instant now);

	List<GreenroomNotificationTarget> findByUserId(UUID userId);
}
