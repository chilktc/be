package be.notification.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import be.common.api.CustomException;
import be.common.api.ErrorCode;
import be.notification.domain.GreenroomNotificationHistory;
import be.notification.domain.SendResult;
import be.notification.repository.GreenroomNotificationHistoryRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GreenroomNotificationQueryService {

	private static final int DEFAULT_SIZE = 20;
	private static final int MAX_SIZE = 100;
	private static final int RECENT_DAYS = 30;

	private final GreenroomNotificationHistoryRepository historyRepository;

	@Transactional(readOnly = true)
	public GreenroomNotificationSliceResponse getNotifications(String userIdHeader, String cursor, Integer size) {
		UUID userId = parseUserId(userIdHeader);
		int pageSize = normalizeSize(size);
		Cursor parsedCursor = parseCursor(cursor);
		Instant from = Instant.now().minus(RECENT_DAYS, ChronoUnit.DAYS);

		List<GreenroomNotificationHistory> histories = parsedCursor == null
			? historyRepository.findRecentByUserId(
				userId,
				SendResult.SUCCESS,
				from,
				PageRequest.of(0, pageSize + 1)
			)
			: historyRepository.findRecentByUserIdBeforeCursor(
				userId,
				SendResult.SUCCESS,
				from,
				parsedCursor.sentAt(),
				parsedCursor.id(),
				PageRequest.of(0, pageSize + 1)
			);

		boolean hasNext = histories.size() > pageSize;
		List<GreenroomNotificationHistory> pageItems = hasNext
			? histories.subList(0, pageSize)
			: histories;

		String nextCursor = hasNext
			? toCursor(pageItems.get(pageItems.size() - 1))
			: null;

		return new GreenroomNotificationSliceResponse(
			pageItems.stream()
				.map(history -> new GreenroomNotificationItemResponse(
					history.getTicketId(),
					calculateDayOffset(history.getSequence())
				))
				.toList(),
			nextCursor,
			hasNext
		);
	}

	private UUID parseUserId(String userIdHeader) {
		try {
			return UUID.fromString(userIdHeader);
		} catch (Exception e) {
			throw new CustomException(ErrorCode.VALIDATION_ERROR);
		}
	}

	private int normalizeSize(Integer size) {
		int requestedSize = size == null ? DEFAULT_SIZE : size;
		if (requestedSize < 1 || requestedSize > MAX_SIZE) {
			throw new CustomException(ErrorCode.VALIDATION_ERROR);
		}
		return requestedSize;
	}

	private Cursor parseCursor(String cursor) {
		if (cursor == null || cursor.isBlank()) {
			return null;
		}

		int separatorIndex = cursor.lastIndexOf('_');
		if (separatorIndex <= 0 || separatorIndex == cursor.length() - 1) {
			throw new CustomException(ErrorCode.VALIDATION_ERROR);
		}

		try {
			return new Cursor(
				Instant.parse(cursor.substring(0, separatorIndex)),
				UUID.fromString(cursor.substring(separatorIndex + 1))
			);
		} catch (Exception e) {
			throw new CustomException(ErrorCode.VALIDATION_ERROR);
		}
	}

	private String toCursor(GreenroomNotificationHistory history) {
		return history.getSentAt() + "_" + history.getId();
	}

	private int calculateDayOffset(int sequence) {
		return switch (sequence) {
			case 1 -> 1;
			case 2 -> 3;
			case 3 -> 7;
			case 4 -> 21;
			default -> 21 + (sequence - 4) * 14;
		};
	}

	private record Cursor(Instant sentAt, UUID id) {
	}

	public record GreenroomNotificationItemResponse(
		UUID ticketId,
		int dayOffset
	) {
	}

	public record GreenroomNotificationSliceResponse(
		List<GreenroomNotificationItemResponse> items,
		String nextCursor,
		boolean hasNext
	) {
	}
}
