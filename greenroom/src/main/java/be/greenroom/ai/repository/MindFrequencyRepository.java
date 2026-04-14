package be.greenroom.ai.repository;

import java.util.UUID;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import be.greenroom.ai.domain.MindFrequency;

public interface MindFrequencyRepository extends JpaRepository<MindFrequency, UUID> {

	Optional<MindFrequency> findBySessionId(String sessionId);
	Optional<MindFrequency> findByTicketId(UUID ticketId);
}
