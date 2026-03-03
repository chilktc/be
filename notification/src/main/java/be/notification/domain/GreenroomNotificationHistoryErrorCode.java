package be.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "greenroom_notification_history_error_code")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GreenroomNotificationHistoryErrorCode {

	@Id
	@Column(name = "id", nullable = false, updatable = false)
	private Long id;

	@MapsId
	@OneToOne(optional = false)
	@JoinColumn(name = "id", nullable = false, updatable = false)
	private GreenroomNotificationHistory history;

	@Column(name = "error_code", nullable = false)
	private String errorCode;

	public static GreenroomNotificationHistoryErrorCode create(
		GreenroomNotificationHistory history,
		String errorCode
	) {
		GreenroomNotificationHistoryErrorCode value = new GreenroomNotificationHistoryErrorCode();
		value.history = history;
		value.errorCode = errorCode;
		return value;
	}
}
