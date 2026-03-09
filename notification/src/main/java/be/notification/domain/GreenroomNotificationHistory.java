package be.notification.domain;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
	name = "greenroom_notification_history",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_history_idempotency_key", columnNames = {"idempotency_key"})
	}
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GreenroomNotificationHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false)
	private UUID userId;

	@Column(name = "ticket_id", nullable = false)
	private UUID ticketId;

	@Column(nullable = false)
	private int sequence;

	@Column(nullable = false)
	private int attempt;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private NotificationChannel channel;

	@Enumerated(EnumType.STRING)
	@Column(name = "template_code", nullable = false)
	private GreenroomTemplateCode templateCode;

	@Column(name = "idempotency_key", nullable = false)
	private String idempotencyKey;

	@Column(name = "sent_at", nullable = false)
	private Instant sentAt;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private SendResult result;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	public static GreenroomNotificationHistory success(
		UUID userId,
		UUID ticketId,
		int sequence,
		int attempt,
		NotificationChannel channel,
		GreenroomTemplateCode templateCode,
		String idempotencyKey,
		Instant sentAt
	) {
		GreenroomNotificationHistory history = new GreenroomNotificationHistory();
		history.userId = userId;
		history.ticketId = ticketId;
		history.sequence = sequence;
		history.attempt = attempt;
		history.channel = channel;
		history.templateCode = templateCode;
		history.idempotencyKey = idempotencyKey;
		history.sentAt = sentAt;
		history.result = SendResult.SUCCESS;
		return history;
	}

	public static GreenroomNotificationHistory fail(
		UUID userId,
		UUID ticketId,
		int sequence,
		int attempt,
		NotificationChannel channel,
		GreenroomTemplateCode templateCode,
		String idempotencyKey,
		Instant sentAt
	) {
		GreenroomNotificationHistory history = success(
			userId,
			ticketId,
			sequence,
			attempt,
			channel,
			templateCode,
			idempotencyKey,
			sentAt
		);
		history.result = SendResult.FAIL;
		return history;
	}

	@PrePersist
	void onCreate() {
		this.createdAt = LocalDateTime.now();
	}
}
