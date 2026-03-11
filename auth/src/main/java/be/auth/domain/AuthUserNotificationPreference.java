package be.auth.domain;

import java.time.LocalDateTime;
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
@Table(name = "auth_user_notification_preference")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuthUserNotificationPreference {

	@Id
	@Column(name = "user_id", nullable = false, updatable = false)
	private UUID userId;

	@Column(nullable = false)
	private boolean enabled;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	public static AuthUserNotificationPreference create(UUID userId, boolean enabled) {
		AuthUserNotificationPreference preference = new AuthUserNotificationPreference();
		preference.userId = userId;
		preference.enabled = enabled;
		return preference;
	}

	public void changeEnabled(boolean enabled) {
		this.enabled = enabled;
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
