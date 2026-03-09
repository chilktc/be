package be.greenroom.tracking.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import be.greenroom.tracking.domain.TrackingRecord;
import be.greenroom.tracking.domain.TrackingStatus;

public interface TrackingRecordRepository extends JpaRepository<TrackingRecord, UUID> {
	List<TrackingRecord> findByTicketIdOrderByCreatedAtDesc(UUID ticketId);
	boolean existsByTicketIdAndStatus(UUID ticketId, TrackingStatus status);
}
