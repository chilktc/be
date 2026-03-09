package be.notification.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import be.notification.domain.GreenroomNotificationHistoryErrorCode;

public interface GreenroomNotificationHistoryErrorCodeRepository
	extends JpaRepository<GreenroomNotificationHistoryErrorCode, Long> {

	List<GreenroomNotificationHistoryErrorCode> findByIdIn(Collection<Long> ids);
}
