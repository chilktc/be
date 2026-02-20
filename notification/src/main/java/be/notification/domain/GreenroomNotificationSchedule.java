package be.notification.domain;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
	name = "greenroom_notification_schedule",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_schedule_ticket_id", columnNames = {"ticket_id"})
	}
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GreenroomNotificationSchedule {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false, updatable = false)
	private UUID userId;

	@Column(name = "ticket_id", nullable = false, updatable = false)
	private UUID ticketId;

	@Column(name = "base_occurred_at", nullable = false)
	private Instant baseOccurredAt;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private NotificationScheduleStatus status;

	@Column(name = "next_sequence", nullable = false)
	private int nextSequence;

	@Column(name = "next_send_at", nullable = false)
	private Instant nextSendAt;

	@Column(name = "preferred_hour", nullable = false)
	private int preferredHour;

	@Column(name = "preferred_minute", nullable = false)
	private int preferredMinute;

	@Column(nullable = false)
	private String timezone;

	@Column(name = "resolved_at")
	private Instant resolvedAt;

	@Column(name = "last_sent_at")
	private Instant lastSentAt;

	@Version
	@Column(nullable = false)
	private long version;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	public static GreenroomNotificationSchedule create(
		UUID userId,
		UUID ticketId,
		Instant baseOccurredAt,
		int preferredHour,
		int preferredMinute,
		String timezone
	) {
		GreenroomNotificationSchedule schedule = new GreenroomNotificationSchedule();
		schedule.userId = userId;
		schedule.ticketId = ticketId;
		schedule.baseOccurredAt = baseOccurredAt;
		schedule.status = NotificationScheduleStatus.ACTIVE;
		schedule.nextSequence = 1;
		schedule.preferredHour = preferredHour;
		schedule.preferredMinute = preferredMinute;
		schedule.timezone = timezone;
		schedule.nextSendAt = schedule.calculateScheduledInstant(1);
		return schedule;
	}

	public void updatePreference(int preferredHour, int preferredMinute, String timezone) {
		this.preferredHour = preferredHour;
		this.preferredMinute = preferredMinute;
		this.timezone = timezone;
		if (this.status == NotificationScheduleStatus.ACTIVE) {
			this.nextSendAt = calculateScheduledInstant(this.nextSequence);
		}
	}

	public void markResolved(Instant resolvedAt) {
		this.status = NotificationScheduleStatus.RESOLVED;
		this.resolvedAt = resolvedAt;
		this.nextSendAt = Instant.MAX;
	}

	public void advanceAfterSend(Instant sentAt) {
		this.lastSentAt = sentAt;
		this.nextSequence = this.nextSequence + 1;
		this.nextSendAt = calculateScheduledInstant(this.nextSequence);
	}

	public void advanceAfterMissed() {
		this.nextSequence = this.nextSequence + 1;
		this.nextSendAt = calculateScheduledInstant(this.nextSequence);
	}

	private Instant calculateScheduledInstant(int sequence) {
		ZonedDateTime firstPreferredDateTime = calculateFirstPreferredDateTime();
		long daysFromFirst = switch (sequence) {
			case 1 -> 0L;
			case 2 -> 3L;
			case 3 -> 7L;
			default -> 21L + (long)(sequence - 4) * 14L;
		};
		return firstPreferredDateTime.plusDays(daysFromFirst).toInstant();
	}

	private ZonedDateTime calculateFirstPreferredDateTime() {
		ZoneId zoneId = ZoneId.of(this.timezone);
		ZonedDateTime base = this.baseOccurredAt.atZone(zoneId);
		LocalTime preferredTime = LocalTime.of(this.preferredHour, this.preferredMinute);
		ZonedDateTime candidate = ZonedDateTime.of(base.toLocalDate(), preferredTime, zoneId);
		if (candidate.isBefore(base)) {
			candidate = candidate.plusDays(1);
		}
		return candidate;
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
