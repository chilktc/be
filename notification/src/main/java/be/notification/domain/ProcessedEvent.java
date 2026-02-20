package be.notification.domain;

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
@Table(name = "processed_event")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProcessedEvent {

	@Id
	@Column(name = "event_id", nullable = false, updatable = false)
	private UUID eventId;

	@Column(name = "event_type", nullable = false)
	private String eventType;

	@Column(name = "processed_at", nullable = false, updatable = false)
	private LocalDateTime processedAt;

	public static ProcessedEvent create(UUID eventId, String eventType) {
		ProcessedEvent processedEvent = new ProcessedEvent();
		processedEvent.eventId = eventId;
		processedEvent.eventType = eventType;
		return processedEvent;
	}

	@PrePersist
	void onCreate() {
		this.processedAt = LocalDateTime.now();
	}
}
