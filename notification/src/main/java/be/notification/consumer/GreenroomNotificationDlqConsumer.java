package be.notification.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class GreenroomNotificationDlqConsumer {

	private static final String DLQ_TOPIC = "greenroom.notification.events.dlq";
	private static final String GROUP_ID = "greenroom-notification-dlq-group";

	@KafkaListener(topics = DLQ_TOPIC, groupId = GROUP_ID)
	public void consume(String message) {
		log.error("[DLQ] greenroom notification event={}", message);
	}
}
