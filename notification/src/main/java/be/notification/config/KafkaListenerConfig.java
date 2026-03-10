package be.notification.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

import com.fasterxml.jackson.core.JsonProcessingException;

@Configuration
public class KafkaListenerConfig {

	private static final String GREENROOM_DLQ_TOPIC = "greenroom.notification.events.dlq";

	@Bean
	public DefaultErrorHandler greenroomNotificationErrorHandler(KafkaTemplate<String, String> kafkaTemplate) {
		ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(3);
		backOff.setInitialInterval(100L);
		backOff.setMultiplier(3.0);
		backOff.setMaxInterval(900L);

		DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
			kafkaTemplate,
			(record, exception) -> new TopicPartition(GREENROOM_DLQ_TOPIC, record.partition())
		);

		DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, backOff);
		handler.addNotRetryableExceptions(JsonProcessingException.class, IllegalArgumentException.class);
		return handler;
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, String> greenroomNotificationKafkaListenerContainerFactory(
		ConsumerFactory<String, String> consumerFactory,
		DefaultErrorHandler greenroomNotificationErrorHandler
	) {
		ConcurrentKafkaListenerContainerFactory<String, String> factory =
			new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory);
		factory.setCommonErrorHandler(greenroomNotificationErrorHandler);
		return factory;
	}

	@Bean
	public NewTopic greenroomNotificationDlqTopic() {
		return new NewTopic(GREENROOM_DLQ_TOPIC, 3, (short)1);
	}
}
