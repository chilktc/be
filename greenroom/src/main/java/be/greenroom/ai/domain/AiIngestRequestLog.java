package be.greenroom.ai.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ai_ingest_request_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiIngestRequestLog {

	@Id
	@Column(nullable = false, updatable = false)
	private UUID id;

	@Column(nullable = false, length = 100)
	private String endpoint;

	@Column(length = 100)
	private String requestType;

	@Column(length = 64)
	private String userId;

	@Column(length = 100)
	private String sessionId;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String payload;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	private AiIngestRequestLog(
		String endpoint,
		String requestType,
		String userId,
		String sessionId,
		String payload
	) {
		this.id = UUID.randomUUID();
		this.endpoint = endpoint;
		this.requestType = requestType;
		this.userId = userId;
		this.sessionId = sessionId;
		this.payload = payload;
	}

	public static AiIngestRequestLog create(
		String endpoint,
		String requestType,
		String userId,
		String sessionId,
		String payload
	) {
		return new AiIngestRequestLog(endpoint, requestType, userId, sessionId, payload);
	}

	@PrePersist
	public void prePersist() {
		if (this.createdAt == null) {
			this.createdAt = LocalDateTime.now();
		}
	}
}
