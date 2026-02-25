package be.greenroom.ticket.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import be.greenroom.ticket.domain.Ticket;
import be.greenroom.ticket.repository.dao.TicketNameCreatedAtDao;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {
    List<Ticket> findByUserIdOrderByCreatedAtDesc(UUID userId);
	@Query("""
        select new be.greenroom.ticket.repository.dao.TicketNameCreatedAtDao(
            t.id,
            t.name,
            t.createdAt
        )
        from Ticket t
        where t.userId = :userId
        order by t.createdAt desc
    """)
	List<TicketNameCreatedAtDao> findNameAndCreatedAtByUserIdOrderByCreatedAtDesc(UUID userId);

	Optional<Ticket> findById(UUID ticketId);
}
