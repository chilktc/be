package be.notification.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import be.notification.domain.GreenroomNotificationTarget;

public interface GreenroomNotificationTargetRepository extends JpaRepository<GreenroomNotificationTarget, UUID> {

	List<GreenroomNotificationTarget> findByResolvedFalseAndEnabledTrueAndNextSendAtLessThanEqual(Instant now);

	List<GreenroomNotificationTarget> findByUserId(UUID userId);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
		update GreenroomNotificationTarget t
		set t.enabled = :enabled
		where t.userId = :userId
	""")
	int updateEnabledByUserId(@Param("userId") UUID userId, @Param("enabled") boolean enabled);
}
