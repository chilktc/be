package be.notification.consumer;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import be.notification.dto.event.GreenroomDifficultyResolvedEvent;
import be.notification.dto.event.GreenroomNotificationPreferenceUpdatedEvent;
import be.notification.dto.event.GreenroomSessionCompletedEvent;
import be.notification.service.GreenroomNotificationScheduleService;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationConsumer 단위 테스트")
class NotificationConsumerTest {

	@Mock
	private GreenroomNotificationScheduleService scheduleService;

	private NotificationConsumer consumer;

	@BeforeEach
	void setUp() {
		consumer = new NotificationConsumer(new ObjectMapper().findAndRegisterModules(), scheduleService);
	}

	@Test
	@DisplayName("session-completed 토픽 메시지는 handleSessionCompleted로 전달된다")
	void consumeSessionCompleted_routesToService() {
		String payload = """
			{
			  \"eventId\": \"11111111-1111-1111-1111-111111111111\",
			  \"occurredAt\": \"2026-03-01T08:00:00Z\",
			  \"userId\": \"22222222-2222-2222-2222-222222222222\",
			  \"ticketId\": \"33333333-3333-3333-3333-333333333333\",
			  \"preferredHour\": 17,
			  \"preferredMinute\": 31,
			  \"timezone\": \"Asia/Seoul\"
			}
			""";

		consumer.consumeSessionCompleted(payload);

		verify(scheduleService).handleSessionCompleted(org.mockito.ArgumentMatchers.any(GreenroomSessionCompletedEvent.class));
	}

	@Test
	@DisplayName("preference-updated 토픽 메시지는 handlePreferenceUpdated로 전달된다")
	void consumePreferenceUpdated_routesToService() {
		String payload = """
			{
			  \"eventId\": \"11111111-1111-1111-1111-111111111111\",
			  \"occurredAt\": \"2026-03-01T08:00:00Z\",
			  \"ticketId\": \"33333333-3333-3333-3333-333333333333\",
			  \"preferredHour\": 22,
			  \"preferredMinute\": 15,
			  \"timezone\": \"Asia/Seoul\"
			}
			""";

		consumer.consumePreferenceUpdated(payload);

		verify(scheduleService).handlePreferenceUpdated(org.mockito.ArgumentMatchers.any(GreenroomNotificationPreferenceUpdatedEvent.class));
	}

	@Test
	@DisplayName("difficulty-resolved 토픽 메시지는 handleResolved로 전달된다")
	void consumeDifficultyResolved_routesToService() {
		String payload = """
			{
			  \"eventId\": \"11111111-1111-1111-1111-111111111111\",
			  \"occurredAt\": \"2026-03-01T08:00:00Z\",
			  \"ticketId\": \"33333333-3333-3333-3333-333333333333\"
			}
			""";

		consumer.consumeDifficultyResolved(payload);

		verify(scheduleService).handleResolved(org.mockito.ArgumentMatchers.any(GreenroomDifficultyResolvedEvent.class));
	}
}
