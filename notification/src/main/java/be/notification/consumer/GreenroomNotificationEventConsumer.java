package be.notification.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import be.notification.event.GreenroomNotificationEventType;
import be.notification.event.GreenroomTicketCreatedEvent;
import be.notification.event.GreenroomTicketResolvedEvent;
import be.notification.event.GreenroomUserNotificationPreferenceUpdatedEvent;
import be.notification.service.GreenroomNotificationEventService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GreenroomNotificationEventConsumer {

	private static final String TOPIC = "greenroom.notification.events";
	private static final String GROUP_ID = "greenroom-notification-group";
	private static final String EVENT_TYPE = "eventType";

	private final ObjectMapper objectMapper;
	private final GreenroomNotificationEventService eventService;

	@KafkaListener(
		topics = TOPIC,
		groupId = GROUP_ID,
		containerFactory = "greenroomNotificationKafkaListenerContainerFactory"
	)
	public void consume(String message) throws Exception {
		JsonNode root = objectMapper.readTree(message);
		GreenroomNotificationEventType eventType = GreenroomNotificationEventType.valueOf(root.get(EVENT_TYPE).asText());
		switch (eventType) {
			case GREENROOM_TICKET_CREATED ->
				eventService.handleTicketCreated(objectMapper.treeToValue(root, GreenroomTicketCreatedEvent.class));
			case GREENROOM_TICKET_RESOLVED ->
				eventService.handleTicketResolved(objectMapper.treeToValue(root, GreenroomTicketResolvedEvent.class));
			case GREENROOM_USER_NOTIFICATION_PREFERENCE_UPDATED -> eventService.handleUserPreferenceUpdated(
				objectMapper.treeToValue(root, GreenroomUserNotificationPreferenceUpdatedEvent.class)
			);
		}
	}
}
