package be.notification.consumer;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.test.context.ActiveProfiles;

import be.NotificationApplication;

@SpringBootTest(properties = {
	"spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
	"spring.kafka.listener.auto-startup=true"
}, classes = {
	NotificationApplication.class,
	GreenroomNotificationKafkaRetryDlqIntegrationTest.TestKafkaConsumersConfig.class
})
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, topics = {
	GreenroomNotificationKafkaRetryDlqIntegrationTest.RETRY_MAIN_TOPIC,
	GreenroomNotificationKafkaRetryDlqIntegrationTest.RETRY_DLQ_TOPIC,
	GreenroomNotificationKafkaRetryDlqIntegrationTest.NON_RETRY_MAIN_TOPIC,
	GreenroomNotificationKafkaRetryDlqIntegrationTest.NON_RETRY_DLQ_TOPIC
})
class GreenroomNotificationKafkaRetryDlqIntegrationTest {

	static final String RETRY_MAIN_TOPIC = "test.notification.retry.events";
	static final String RETRY_DLQ_TOPIC = "test.notification.retry.events.dlq";
	static final String NON_RETRY_MAIN_TOPIC = "test.notification.non-retry.events";
	static final String NON_RETRY_DLQ_TOPIC = "test.notification.non-retry.events.dlq";

	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;

	@Autowired
	private RetryableFailingConsumer retryableFailingConsumer;

	@Autowired
	private NonRetryableFailingConsumer nonRetryableFailingConsumer;

	@Autowired
	private RetryDlqProbeConsumer retryDlqProbeConsumer;

	@Autowired
	private NonRetryDlqProbeConsumer nonRetryDlqProbeConsumer;

	@BeforeEach
	void setUp() {
		retryableFailingConsumer.reset();
		nonRetryableFailingConsumer.reset();
		retryDlqProbeConsumer.reset();
		nonRetryDlqProbeConsumer.reset();
	}

	@Test
	void retryableException_3회재시도후_DLQ로이동한다() throws Exception {
		// given
		kafkaTemplate.send(RETRY_MAIN_TOPIC, "retryable-test-message").get(3, TimeUnit.SECONDS);

		// when
		waitUntil(
			() -> retryableFailingConsumer.attempts() == 4 && retryDlqProbeConsumer.receivedCount() == 1,
			Duration.ofSeconds(10)
		);

		// then
		assertThat(retryableFailingConsumer.attempts()).isEqualTo(4);
		assertThat(retryDlqProbeConsumer.receivedCount()).isEqualTo(1);
		assertThat(retryDlqProbeConsumer.lastOriginalTopic()).isEqualTo(RETRY_MAIN_TOPIC);
	}

	@Test
	void nonRetryableException_재시도없이_DLQ로이동한다() throws Exception {
		// given
		kafkaTemplate.send(NON_RETRY_MAIN_TOPIC, "non-retryable-test-message").get(3, TimeUnit.SECONDS);

		// when
		waitUntil(
			() -> nonRetryableFailingConsumer.attempts() == 1 && nonRetryDlqProbeConsumer.receivedCount() == 1,
			Duration.ofSeconds(10)
		);

		// then
		assertThat(nonRetryableFailingConsumer.attempts()).isEqualTo(1);
		assertThat(nonRetryDlqProbeConsumer.receivedCount()).isEqualTo(1);
		assertThat(nonRetryDlqProbeConsumer.lastOriginalTopic()).isEqualTo(NON_RETRY_MAIN_TOPIC);
	}

	private void waitUntil(Check condition, Duration timeout) throws InterruptedException {
		long deadline = System.currentTimeMillis() + timeout.toMillis();
		while (System.currentTimeMillis() < deadline) {
			if (condition.ok()) {
				return;
			}
			Thread.sleep(100);
		}
		throw new AssertionError("Condition was not satisfied within " + timeout);
	}

	@FunctionalInterface
	interface Check {
		boolean ok();
	}

	static class RetryableFailingConsumer {
		private final AtomicInteger attempts = new AtomicInteger();

		@KafkaListener(
			topics = RETRY_MAIN_TOPIC,
			groupId = "retryable-failing-group",
			containerFactory = "retryableKafkaListenerContainerFactory"
		)
		public void consume(String message) {
			attempts.incrementAndGet();
			throw new RuntimeException("retryable failure");
		}

		int attempts() {
			return attempts.get();
		}

		void reset() {
			attempts.set(0);
		}
	}

	static class NonRetryableFailingConsumer {
		private final AtomicInteger attempts = new AtomicInteger();

		@KafkaListener(
			topics = NON_RETRY_MAIN_TOPIC,
			groupId = "non-retryable-failing-group",
			containerFactory = "nonRetryableKafkaListenerContainerFactory"
		)
		public void consume(String message) {
			attempts.incrementAndGet();
			throw new IllegalArgumentException("non-retryable failure");
		}

		int attempts() {
			return attempts.get();
		}

		void reset() {
			attempts.set(0);
		}
	}

	static class RetryDlqProbeConsumer {
		private final AtomicInteger receivedCount = new AtomicInteger();
		private volatile String lastOriginalTopic;

		@KafkaListener(topics = RETRY_DLQ_TOPIC, groupId = "retry-dlq-probe-group")
		public void consume(String message, @Header(name = KafkaHeaders.DLT_ORIGINAL_TOPIC, required = false) String originalTopic) {
			receivedCount.incrementAndGet();
			lastOriginalTopic = originalTopic;
		}

		int receivedCount() {
			return receivedCount.get();
		}

		String lastOriginalTopic() {
			return lastOriginalTopic;
		}

		void reset() {
			receivedCount.set(0);
			lastOriginalTopic = null;
		}
	}

	static class NonRetryDlqProbeConsumer {
		private final AtomicInteger receivedCount = new AtomicInteger();
		private volatile String lastOriginalTopic;

		@KafkaListener(topics = NON_RETRY_DLQ_TOPIC, groupId = "non-retry-dlq-probe-group")
		public void consume(String message, @Header(name = KafkaHeaders.DLT_ORIGINAL_TOPIC, required = false) String originalTopic) {
			receivedCount.incrementAndGet();
			lastOriginalTopic = originalTopic;
		}

		int receivedCount() {
			return receivedCount.get();
		}

		String lastOriginalTopic() {
			return lastOriginalTopic;
		}

		void reset() {
			receivedCount.set(0);
			lastOriginalTopic = null;
		}
	}

	@Configuration
	static class TestKafkaConsumersConfig {
		@Bean
		DefaultErrorHandler retryableKafkaErrorHandler(KafkaTemplate<String, String> kafkaTemplate) {
			ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(3);
			backOff.setInitialInterval(100L);
			backOff.setMultiplier(3.0);
			backOff.setMaxInterval(900L);

			DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
				kafkaTemplate,
				(record, exception) -> new org.apache.kafka.common.TopicPartition(RETRY_DLQ_TOPIC, record.partition())
			);
			return new DefaultErrorHandler(recoverer, backOff);
		}

		@Bean
		DefaultErrorHandler nonRetryableKafkaErrorHandler(KafkaTemplate<String, String> kafkaTemplate) {
			ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(3);
			backOff.setInitialInterval(100L);
			backOff.setMultiplier(3.0);
			backOff.setMaxInterval(900L);

			DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
				kafkaTemplate,
				(record, exception) -> new org.apache.kafka.common.TopicPartition(NON_RETRY_DLQ_TOPIC, record.partition())
			);
			DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, backOff);
			handler.addNotRetryableExceptions(IllegalArgumentException.class);
			return handler;
		}

		@Bean
		ConcurrentKafkaListenerContainerFactory<String, String> retryableKafkaListenerContainerFactory(
			ConsumerFactory<String, String> consumerFactory,
			DefaultErrorHandler retryableKafkaErrorHandler
		) {
			ConcurrentKafkaListenerContainerFactory<String, String> factory =
				new ConcurrentKafkaListenerContainerFactory<>();
			factory.setConsumerFactory(consumerFactory);
			factory.setCommonErrorHandler(retryableKafkaErrorHandler);
			return factory;
		}

		@Bean
		ConcurrentKafkaListenerContainerFactory<String, String> nonRetryableKafkaListenerContainerFactory(
			ConsumerFactory<String, String> consumerFactory,
			DefaultErrorHandler nonRetryableKafkaErrorHandler
		) {
			ConcurrentKafkaListenerContainerFactory<String, String> factory =
				new ConcurrentKafkaListenerContainerFactory<>();
			factory.setConsumerFactory(consumerFactory);
			factory.setCommonErrorHandler(nonRetryableKafkaErrorHandler);
			return factory;
		}

		@Bean
		RetryableFailingConsumer retryableFailingConsumer() {
			return new RetryableFailingConsumer();
		}

		@Bean
		NonRetryableFailingConsumer nonRetryableFailingConsumer() {
			return new NonRetryableFailingConsumer();
		}

		@Bean
		RetryDlqProbeConsumer retryDlqProbeConsumer() {
			return new RetryDlqProbeConsumer();
		}

		@Bean
		NonRetryDlqProbeConsumer nonRetryDlqProbeConsumer() {
			return new NonRetryDlqProbeConsumer();
		}
	}
}
