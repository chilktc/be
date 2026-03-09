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
@Table(name = "greenroom_notification_processed_event")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GreenroomNotificationProcessedEvent {

	@Id
	@Column(name = "event_id", nullable = false, updatable = false)
	private UUID eventId;

	@Column(name = "event_type", nullable = false)
	private String eventType;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	public static GreenroomNotificationProcessedEvent create(UUID eventId, String eventType) {
		GreenroomNotificationProcessedEvent event = new GreenroomNotificationProcessedEvent();
		event.eventId = eventId;
		event.eventType = eventType;
		return event;
	}

	@PrePersist
	void onCreate() {
		this.createdAt = LocalDateTime.now();
	}
}
