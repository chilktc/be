package be.notification.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import be.notification.domain.GreenroomEventType;
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
	private static final String GREENROOM_NOTIFICATION_TOPIC = "greenroom.notification.events";
	private static final String EVENT_TYPE = "eventType";

	private final ObjectMapper objectMapper;
	private final GreenroomNotificationScheduleService scheduleService;

	@KafkaListener(topics = GREENROOM_NOTIFICATION_TOPIC, groupId = GREENROOM_NOTIFICATION_GROUP)
	public void consume(String message) {
		try {
			JsonNode root = objectMapper.readTree(message);
			GreenroomEventType eventType = GreenroomEventType.valueOf(root.get(EVENT_TYPE).asText());
			switch (eventType) {
				case GREENROOM_SESSION_COMPLETED ->
					scheduleService.handleSessionCompleted(
						objectMapper.treeToValue(root, GreenroomSessionCompletedEvent.class)
					);
				case GREENROOM_NOTIFICATION_PREFERENCE_UPDATED ->
					scheduleService.handlePreferenceUpdated(
						objectMapper.treeToValue(root, GreenroomNotificationPreferenceUpdatedEvent.class)
					);
				case GREENROOM_DIFFICULTY_RESOLVED ->
					scheduleService.handleResolved(
						objectMapper.treeToValue(root, GreenroomDifficultyResolvedEvent.class)
					);
			}
		} catch (JsonProcessingException exception) {
			log.error("Failed to parse notification event payload={}", message, exception);
		} catch (Exception exception) {
			log.error("Failed to consume notification event payload={}", message, exception);
		}
	}
}
