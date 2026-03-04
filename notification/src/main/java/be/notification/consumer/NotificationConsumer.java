package be.notification.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import be.notification.dto.event.GreenroomDifficultyResolvedEvent;
import be.notification.dto.event.GreenroomNotificationPreferenceUpdatedEvent;
import be.notification.dto.event.GreenroomSessionCompletedEvent;
import be.notification.service.GreenroomNotificationScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

	private static final String GREENROOM_NOTIFICATION_GROUP = "greenroom-notification-group";
	private static final String TOPIC_SESSION_COMPLETED = "greenroom.notification.session-completed";
	private static final String TOPIC_PREFERENCE_UPDATED = "greenroom.notification.preference-updated";
	private static final String TOPIC_DIFFICULTY_RESOLVED = "greenroom.notification.difficulty-resolved";

	private final ObjectMapper objectMapper;
	private final GreenroomNotificationScheduleService scheduleService;

	@KafkaListener(topics = TOPIC_SESSION_COMPLETED, groupId = GREENROOM_NOTIFICATION_GROUP)
	public void consumeSessionCompleted(String message) {
		try {
			scheduleService.handleSessionCompleted(objectMapper.readValue(message, GreenroomSessionCompletedEvent.class));
		} catch (JsonProcessingException exception) {
			log.error("Failed to parse session-completed payload={}", message, exception);
		} catch (Exception exception) {
			log.error("Failed to consume session-completed payload={}", message, exception);
		}
	}

	@KafkaListener(topics = TOPIC_PREFERENCE_UPDATED, groupId = GREENROOM_NOTIFICATION_GROUP)
	public void consumePreferenceUpdated(String message) {
		try {
			scheduleService.handlePreferenceUpdated(objectMapper.readValue(message, GreenroomNotificationPreferenceUpdatedEvent.class));
		} catch (JsonProcessingException exception) {
			log.error("Failed to parse preference-updated payload={}", message, exception);
		} catch (Exception exception) {
			log.error("Failed to consume preference-updated payload={}", message, exception);
		}
	}

	@KafkaListener(topics = TOPIC_DIFFICULTY_RESOLVED, groupId = GREENROOM_NOTIFICATION_GROUP)
	public void consumeDifficultyResolved(String message) {
		try {
			scheduleService.handleResolved(objectMapper.readValue(message, GreenroomDifficultyResolvedEvent.class));
		} catch (JsonProcessingException exception) {
			log.error("Failed to parse difficulty-resolved payload={}", message, exception);
		} catch (Exception exception) {
			log.error("Failed to consume difficulty-resolved payload={}", message, exception);
		}
	}
}
