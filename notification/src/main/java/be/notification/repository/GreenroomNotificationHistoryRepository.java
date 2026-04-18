package be.notification.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import be.notification.domain.GreenroomNotificationHistory;
import be.notification.domain.SendResult;

import java.time.Instant;

public interface GreenroomNotificationHistoryRepository extends JpaRepository<GreenroomNotificationHistory, UUID> {

	boolean existsByIdempotencyKey(String idempotencyKey);

	List<GreenroomNotificationHistory> findByIdempotencyKeyIn(Collection<String> idempotencyKeys);

	long countByIdempotencyKeyIn(Collection<String> idempotencyKeys);

	long deleteByIdempotencyKeyIn(Collection<String> idempotencyKeys);

	long deleteByTicketId(UUID ticketId);

	@Query("""
		select h
		from GreenroomNotificationHistory h
		where h.userId = :userId
		  and h.result = :result
		  and h.sentAt >= :from
		order by h.sentAt desc, h.id desc
		""")
	List<GreenroomNotificationHistory> findRecentByUserId(
		@Param("userId") UUID userId,
		@Param("result") SendResult result,
		@Param("from") Instant from,
		Pageable pageable
	);

	@Query("""
		select h
		from GreenroomNotificationHistory h
		where h.userId = :userId
		  and h.result = :result
		  and h.sentAt >= :from
		  and (h.sentAt < :cursorSentAt or (h.sentAt = :cursorSentAt and h.id < :cursorId))
		order by h.sentAt desc, h.id desc
		""")
	List<GreenroomNotificationHistory> findRecentByUserIdBeforeCursor(
		@Param("userId") UUID userId,
		@Param("result") SendResult result,
		@Param("from") Instant from,
		@Param("cursorSentAt") Instant cursorSentAt,
		@Param("cursorId") UUID cursorId,
		Pageable pageable
	);
}
