package be.greenroom.notification.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import be.common.api.CustomException;
import be.common.api.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GreenroomNotificationEventProducer {

	private static final String TOPIC_SESSION_COMPLETED = "greenroom.notification.session-completed";
	private static final String TOPIC_PREFERENCE_UPDATED = "greenroom.notification.preference-updated";
	private static final String TOPIC_DIFFICULTY_RESOLVED = "greenroom.notification.difficulty-resolved";

	private final KafkaTemplate<String, String> kafkaTemplate;
	private final ObjectMapper objectMapper;

	public void publishSessionCompleted(Object event) {
		publish(TOPIC_SESSION_COMPLETED, event);
	}

	public void publishPreferenceUpdated(Object event) {
		publish(TOPIC_PREFERENCE_UPDATED, event);
	}

	public void publishDifficultyResolved(Object event) {
		publish(TOPIC_DIFFICULTY_RESOLVED, event);
	}

	private void publish(String topic, Object event) {
		try {
			String payload = objectMapper.writeValueAsString(event);
			kafkaTemplate.send(topic, payload);
		} catch (JsonProcessingException exception) {
			throw new CustomException(ErrorCode.NOTIFICATION_EVENT_SERIALIZATION_FAILED);
		}
	}
}
