package be.notification.domain;

import java.time.Instant;
import java.time.LocalDateTime;

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

	@Column(name = "schedule_id", nullable = false)
	private Long scheduleId;

	@Column(nullable = false)
	private int sequence;

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

	@Column(name = "error_code")
	private String errorCode;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	public static GreenroomNotificationHistory success(
		Long scheduleId,
		int sequence,
		NotificationChannel channel,
		GreenroomTemplateCode templateCode,
		String idempotencyKey,
		Instant sentAt
	) {
		GreenroomNotificationHistory history = new GreenroomNotificationHistory();
		history.scheduleId = scheduleId;
		history.sequence = sequence;
		history.channel = channel;
		history.templateCode = templateCode;
		history.idempotencyKey = idempotencyKey;
		history.sentAt = sentAt;
		history.result = SendResult.SUCCESS;
		return history;
	}

	public static GreenroomNotificationHistory fail(
		Long scheduleId,
		int sequence,
		NotificationChannel channel,
		GreenroomTemplateCode templateCode,
		String idempotencyKey,
		Instant sentAt,
		String errorCode
	) {
		GreenroomNotificationHistory history = success(
			scheduleId,
			sequence,
			channel,
			templateCode,
			idempotencyKey,
			sentAt
		);
		history.result = SendResult.FAIL;
		history.errorCode = errorCode;
		return history;
	}

	@PrePersist
	void onCreate() {
		this.createdAt = LocalDateTime.now();
	}
}
