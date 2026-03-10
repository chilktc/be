package be.greenroom.tracking.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import be.greenroom.tracking.domain.Tracking;
import be.greenroom.tracking.domain.TrackingStatus;

public interface TrackingRepository extends JpaRepository<Tracking, UUID> {
	List<Tracking> findByTicketIdOrderByCreatedAtDesc(UUID ticketId);
	boolean existsByTicketIdAndStatus(UUID ticketId, TrackingStatus status);
}
