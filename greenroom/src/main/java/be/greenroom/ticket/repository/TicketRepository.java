package be.greenroom.ticket.repository;

import java.util.List;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import be.greenroom.ticket.domain.Ticket;
import be.greenroom.ticket.repository.dao.TicketPreviewDao;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {

	Optional<Ticket> findBySessionId(String sessionId);

	@Query("""
        select new be.greenroom.ticket.repository.dao.TicketPreviewDao(
            t.id,
            t.name,
            t.createdAt
        )
        from Ticket t
        where t.userId = :userId
          and t.name is not null
          and (:cursorCreatedAt is null or t.createdAt < :cursorCreatedAt)
        order by t.createdAt desc
    """)
	List<TicketPreviewDao> findTicketSlice(
		UUID userId,
		LocalDateTime cursorCreatedAt,
		Pageable pageable
	);
}
