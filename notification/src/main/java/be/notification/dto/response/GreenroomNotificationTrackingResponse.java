package be.notification.dto.response;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import be.notification.domain.GreenroomNotificationHistory;
import be.notification.domain.GreenroomNotificationHistoryErrorCode;
import be.notification.domain.GreenroomNotificationSchedule;
import be.notification.domain.NotificationChannel;
import be.notification.domain.NotificationScheduleStatus;
import be.notification.domain.SendResult;

public record GreenroomNotificationTrackingResponse(
	UUID userId,
	List<ScheduleItem> schedules
) {
	public record ScheduleItem(
		Long id,
		UUID ticketId,
		NotificationScheduleStatus status,
		int nextSequence,
		Instant nextSendAt,
		int preferredHour,
		int preferredMinute,
		String timezone,
		Instant resolvedAt,
		Instant lastSentAt,
		Instant baseOccurredAt,
		LocalDateTime createdAt,
		LocalDateTime updatedAt,
		List<HistoryItem> histories
	) {
		public static ScheduleItem of(
			GreenroomNotificationSchedule schedule,
			List<HistoryItem> histories
		) {
			return new ScheduleItem(
				schedule.getId(),
				schedule.getTicketId(),
				schedule.getStatus(),
				schedule.getNextSequence(),
				schedule.getNextSendAt(),
				schedule.getPreferredHour(),
				schedule.getPreferredMinute(),
				schedule.getTimezone(),
				schedule.getResolvedAt(),
				schedule.getLastSentAt(),
				schedule.getBaseOccurredAt(),
				schedule.getCreatedAt(),
				schedule.getUpdatedAt(),
				histories
			);
		}
	}

	public record HistoryItem(
		Long id,
		Long scheduleId,
		int sequence,
		NotificationChannel channel,
		String templateCode,
		Instant sentAt,
		SendResult result,
		HistoryErrorCodeItem errorCode
	) {
		public static HistoryItem of(
			GreenroomNotificationHistory history,
			GreenroomNotificationHistoryErrorCode errorCode
		) {
			return new HistoryItem(
				history.getId(),
				history.getScheduleId(),
				history.getSequence(),
				history.getChannel(),
				history.getTemplateCode().name(),
				history.getSentAt(),
				history.getResult(),
				errorCode == null ? null : HistoryErrorCodeItem.of(errorCode)
			);
		}
	}

	public record HistoryErrorCodeItem(
		Long historyId,
		String errorCode
	) {
		public static HistoryErrorCodeItem of(GreenroomNotificationHistoryErrorCode errorCode) {
			return new HistoryErrorCodeItem(errorCode.getId(), errorCode.getErrorCode());
		}
	}
}
