package be.notification.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import be.notification.domain.GreenroomNotificationHistory;
import be.notification.domain.GreenroomNotificationHistoryErrorCode;
import be.notification.domain.GreenroomNotificationSchedule;
import be.notification.dto.response.GreenroomNotificationTrackingResponse;
import be.notification.repository.GreenroomNotificationHistoryErrorCodeRepository;
import be.notification.repository.GreenroomNotificationHistoryRepository;
import be.notification.repository.GreenroomNotificationScheduleRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GreenroomNotificationQueryService {

	private final GreenroomNotificationScheduleRepository scheduleRepository;
	private final GreenroomNotificationHistoryRepository historyRepository;
	private final GreenroomNotificationHistoryErrorCodeRepository historyErrorCodeRepository;

	@Transactional(readOnly = true)
	public GreenroomNotificationTrackingResponse getTrackingByUserId(UUID userId) {
		List<GreenroomNotificationSchedule> schedules = scheduleRepository.findByUserIdOrderByCreatedAtDesc(userId);
		if (schedules.isEmpty()) {
			return new GreenroomNotificationTrackingResponse(userId, List.of());
		}

		List<Long> scheduleIds = schedules.stream().map(GreenroomNotificationSchedule::getId).toList();
		List<GreenroomNotificationHistory> histories = historyRepository.findByScheduleIdInOrderByIdAsc(scheduleIds);

		Map<Long, GreenroomNotificationHistoryErrorCode> errorCodeByHistoryId = findErrorCodeByHistoryId(histories);
		Map<Long, List<GreenroomNotificationTrackingResponse.HistoryItem>> historyItemsByScheduleId = histories.stream()
			.map(history -> GreenroomNotificationTrackingResponse.HistoryItem.of(
				history,
				errorCodeByHistoryId.get(history.getId())
			))
			.collect(Collectors.groupingBy(
				GreenroomNotificationTrackingResponse.HistoryItem::scheduleId
			));

		List<GreenroomNotificationTrackingResponse.ScheduleItem> scheduleItems = schedules.stream()
			.map(schedule -> GreenroomNotificationTrackingResponse.ScheduleItem.of(
				schedule,
				historyItemsByScheduleId.getOrDefault(schedule.getId(), Collections.emptyList())
			))
			.toList();

		return new GreenroomNotificationTrackingResponse(userId, scheduleItems);
	}

	private Map<Long, GreenroomNotificationHistoryErrorCode> findErrorCodeByHistoryId(
		List<GreenroomNotificationHistory> histories
	) {
		if (histories.isEmpty()) {
			return Map.of();
		}
		List<Long> historyIds = histories.stream().map(GreenroomNotificationHistory::getId).toList();
		return historyErrorCodeRepository.findByIdIn(historyIds)
			.stream()
			.collect(Collectors.toMap(
				GreenroomNotificationHistoryErrorCode::getId,
				Function.identity()
			));
	}
}
