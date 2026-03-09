package be.greenroom.notification.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import be.common.api.CustomException;
import be.common.api.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GreenroomNotificationEventPublisher {

	private static final String GREENROOM_NOTIFICATION_TOPIC = "greenroom.notification.events";

	private final KafkaTemplate<String, String> kafkaTemplate;
	private final ObjectMapper objectMapper;

	public void publish(String key, Object event) {
		// TODO: 이벤트 발행 책임의 서비스 위치를 추후 재검토 (greenroom 유지 vs 별도 서비스)
		try {
			String payload = objectMapper.writeValueAsString(event);
			kafkaTemplate.send(GREENROOM_NOTIFICATION_TOPIC, key, payload);
		} catch (JsonProcessingException exception) {
			throw new CustomException(ErrorCode.NOTIFICATION_EVENT_SERIALIZATION_FAILED);
		}
	}
}
