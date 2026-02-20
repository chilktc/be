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

	private static final String GREENROOM_NOTIFICATION_TOPIC = "greenroom.notification.events";

	private final KafkaTemplate<String, String> kafkaTemplate;
	private final ObjectMapper objectMapper;

	public void publish(Object event) {
		try {
			String payload = objectMapper.writeValueAsString(event);
			kafkaTemplate.send(GREENROOM_NOTIFICATION_TOPIC, payload);
		} catch (JsonProcessingException exception) {
			throw new CustomException(ErrorCode.NOTIFICATION_EVENT_SERIALIZATION_FAILED);
		}
	}
}
