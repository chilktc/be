package be.greenroom.ai.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import be.greenroom.ai.domain.AiIngestRequestLog;

public interface AiIngestRequestLogRepository extends JpaRepository<AiIngestRequestLog, UUID> {
	void deleteAllBySessionId(String sessionId);
}
