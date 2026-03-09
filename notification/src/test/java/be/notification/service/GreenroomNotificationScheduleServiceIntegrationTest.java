package be.notification.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import be.notification.domain.GreenroomNotificationHistory;
import be.notification.domain.GreenroomNotificationHistoryErrorCode;
import be.notification.domain.GreenroomNotificationSchedule;
import be.notification.domain.NotificationScheduleStatus;
import be.notification.domain.SendResult;
import be.notification.repository.GreenroomNotificationHistoryRepository;
import be.notification.repository.GreenroomNotificationHistoryErrorCodeRepository;
import be.notification.repository.GreenroomNotificationScheduleRepository;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@TestPropertySource(properties = {
	"spring.kafka.listener.auto-startup=false"
})
class GreenroomNotificationScheduleServiceIntegrationTest {

	@Autowired
	private GreenroomNotificationScheduleService scheduleService;

	@Autowired
	private GreenroomNotificationScheduleRepository scheduleRepository;

	@Autowired
	private GreenroomNotificationHistoryRepository historyRepository;

	@Autowired
	private GreenroomNotificationHistoryErrorCodeRepository historyErrorCodeRepository;

	@BeforeEach
	void setUp() {
		historyErrorCodeRepository.deleteAll();
		historyRepository.deleteAll();
		scheduleRepository.deleteAll();
	}

	@Test
	@DisplayName("DB 기준으로 -5,-3,-2,-1,0,1,2,3,5분 오프셋이 overdue/due/미대상으로 정확히 처리된다")
	void 오프셋_경계값별_스케줄_처리결과_검증() {
		// given
		Instant fixedNow = Instant.parse("2026-03-03T12:00:00Z");

		Map<Integer, GreenroomNotificationSchedule> scheduleByOffset = new LinkedHashMap<>();
		List<Integer> offsets = List.of(-5, -3, -2, -1, 0, 1, 2, 3, 5);
		for (Integer offset : offsets) {
			GreenroomNotificationSchedule schedule = createActiveScheduleWithOffset(fixedNow, offset);
			scheduleByOffset.put(offset, schedule);
		}

		// when
		try (MockedStatic<Instant> mockedInstant = Mockito.mockStatic(Instant.class, Mockito.CALLS_REAL_METHODS)) {
			mockedInstant.when(Instant::now).thenReturn(fixedNow);

			scheduleService.sendDueSchedules();
		}

		Map<Long, GreenroomNotificationHistory> historyByScheduleId = historyRepository.findAll()
			.stream()
			.collect(java.util.stream.Collectors.toMap(
				GreenroomNotificationHistory::getScheduleId,
				h -> h
			));

		// then
		// -5분: overdue -> FAIL + errorCode
		assertHistoryResult(historyByScheduleId, scheduleByOffset.get(-5), SendResult.FAIL, "MISSED_AFTER_3_MIN");

		// -3 ~ +3분: due -> SUCCESS
		assertHistoryResult(historyByScheduleId, scheduleByOffset.get(-3), SendResult.SUCCESS, null);
		assertHistoryResult(historyByScheduleId, scheduleByOffset.get(-2), SendResult.SUCCESS, null);
		assertHistoryResult(historyByScheduleId, scheduleByOffset.get(-1), SendResult.SUCCESS, null);
		assertHistoryResult(historyByScheduleId, scheduleByOffset.get(0), SendResult.SUCCESS, null);
		assertHistoryResult(historyByScheduleId, scheduleByOffset.get(1), SendResult.SUCCESS, null);
		assertHistoryResult(historyByScheduleId, scheduleByOffset.get(2), SendResult.SUCCESS, null);
		assertHistoryResult(historyByScheduleId, scheduleByOffset.get(3), SendResult.SUCCESS, null);

		// +5분: 처리 대상 아님 -> history 없음
		assertThat(historyByScheduleId.containsKey(scheduleByOffset.get(5).getId())).isFalse();

		// 처리된 항목(-5, -3 ~ +3)은 nextSequence 2, 미처리(+5)는 1 유지
		for (Integer offset : offsets) {
			GreenroomNotificationSchedule reloaded = scheduleRepository.findById(scheduleByOffset.get(offset).getId())
				.orElseThrow();
			if (offset == 5) {
				assertThat(reloaded.getNextSequence()).isEqualTo(1);
			} else {
				assertThat(reloaded.getNextSequence()).isEqualTo(2);
			}
			assertThat(reloaded.getStatus()).isEqualTo(NotificationScheduleStatus.ACTIVE);
		}
	}

	private GreenroomNotificationSchedule createActiveScheduleWithOffset(Instant fixedNow, int offsetMinutes) {
		GreenroomNotificationSchedule schedule = GreenroomNotificationSchedule.create(
			UUID.randomUUID(),
			UUID.randomUUID(),
			fixedNow.minus(2, ChronoUnit.DAYS),
			19,
			0,
			"Asia/Seoul"
		);
		ReflectionTestUtils.setField(schedule, "nextSendAt", fixedNow.plus(offsetMinutes, ChronoUnit.MINUTES));
		ReflectionTestUtils.setField(schedule, "nextSequence", 1);
		return scheduleRepository.save(schedule);
	}

	private void assertHistoryResult(
		Map<Long, GreenroomNotificationHistory> historyByScheduleId,
		GreenroomNotificationSchedule schedule,
		SendResult expectedResult,
		String expectedErrorCode
	) {
		GreenroomNotificationHistory history = historyByScheduleId.get(schedule.getId());
		assertThat(history).isNotNull();
		assertThat(history.getResult()).isEqualTo(expectedResult);

		if (expectedErrorCode == null) {
			assertThat(historyErrorCodeRepository.findById(history.getId())).isEmpty();
		} else {
			GreenroomNotificationHistoryErrorCode value = historyErrorCodeRepository.findById(history.getId())
				.orElseThrow();
			assertThat(value.getErrorCode()).isEqualTo(expectedErrorCode);
		}
	}
}
