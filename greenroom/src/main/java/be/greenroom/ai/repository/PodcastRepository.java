package be.greenroom.ai.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import be.greenroom.ai.domain.Podcast;

public interface PodcastRepository extends JpaRepository<Podcast, UUID> {
	Optional<Podcast> findBySessionId(String sessionId);
}
