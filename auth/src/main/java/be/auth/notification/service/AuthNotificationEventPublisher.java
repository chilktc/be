package be.auth.notification.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import be.auth.notification.event.GreenroomNotificationEventType;
import be.auth.notification.event.GreenroomUserNotificationPreferenceUpdatedEvent;
import be.common.api.CustomException;
import be.common.api.ErrorCode;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthNotificationEventPublisher {

	private static final String TOPIC = "greenroom.notification.events";

	private final KafkaTemplate<String, String> kafkaTemplate;
	private final ObjectMapper objectMapper;

	public void publishUserPreferenceUpdated(UUID userId, boolean enabled) {
		GreenroomUserNotificationPreferenceUpdatedEvent event =
			new GreenroomUserNotificationPreferenceUpdatedEvent(
				UUID.randomUUID(),
				GreenroomNotificationEventType.GREENROOM_USER_NOTIFICATION_PREFERENCE_UPDATED.name(),
				LocalDateTime.now(),
				userId,
				enabled
			);
		try {
			kafkaTemplate.send(TOPIC, userId.toString(), objectMapper.writeValueAsString(event));
		} catch (JsonProcessingException exception) {
			throw new CustomException(ErrorCode.NOTIFICATION_EVENT_SERIALIZATION_FAILED);
		}
	}
}
