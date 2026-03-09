package be.notification.domain;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "greenroom_notification_target")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GreenroomNotificationTarget {

	private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");
	private static final LocalTime SEND_TIME = LocalTime.of(8, 30);

	@Id
	@Column(name = "ticket_id", nullable = false, updatable = false)
	private UUID ticketId;

	@Column(name = "user_id", nullable = false)
	private UUID userId;

	@Column(name = "ticket_created_at", nullable = false)
	private LocalDateTime ticketCreatedAt;

	@Column(nullable = false)
	private boolean enabled;

	@Column(nullable = false)
	private boolean resolved;

	@Column(name = "next_sequence", nullable = false)
	private int nextSequence;

	@Column(name = "next_send_at", nullable = false)
	private Instant nextSendAt;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	public static GreenroomNotificationTarget create(
		UUID ticketId,
		UUID userId,
		LocalDateTime ticketCreatedAt,
		boolean enabled
	) {
		GreenroomNotificationTarget target = new GreenroomNotificationTarget();
		target.ticketId = ticketId;
		target.userId = userId;
		target.ticketCreatedAt = ticketCreatedAt;
		target.enabled = enabled;
		target.resolved = false;
		target.nextSequence = 1;
		target.nextSendAt = target.calculateNextSendAt(1);
		return target;
	}

	public void resolve() {
		this.resolved = true;
	}

	public void changeEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void advanceAfterSuccess() {
		this.nextSequence = this.nextSequence + 1;
		this.nextSendAt = calculateNextSendAt(this.nextSequence);
	}

	private Instant calculateNextSendAt(int sequence) {
		long offsetDays = switch (sequence) {
			case 1 -> 1L;
			case 2 -> 3L;
			case 3 -> 7L;
			case 4 -> 21L;
			default -> 21L + (long) (sequence - 4) * 14L;
		};
		return ticketCreatedAt.atZone(SEOUL_ZONE)
			.toLocalDate()
			.plusDays(offsetDays)
			.atTime(SEND_TIME)
			.atZone(SEOUL_ZONE)
			.toInstant();
	}

	@PrePersist
	void onCreate() {
		LocalDateTime now = LocalDateTime.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}
}
