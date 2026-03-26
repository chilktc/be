package be.notification.consumer;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = "notification.dispatch.parallel-consumer.enabled=true")
@EmbeddedKafka(partitions = 1, topics = {"greenroom.notification.dispatch", "greenroom.notification.dispatch.dlq"})
@ActiveProfiles("test")
@DirtiesContext
class GreenroomNotificationKafkaRetryDlqIntegrationTest {

	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;

	@Autowired
	private EmbeddedKafkaBroker embeddedKafkaBroker;

	@Test
	@DisplayName("parallel-consumer 처리 실패 메시지는 DLQ로 이동한다")
	void 처리실패메시지는_DLQ로_이동한다() {
		String invalidMessage = "{\"ticketId\":";
		String key = UUID.randomUUID().toString();

		Map<String, Object> props = KafkaTestUtils.consumerProps("notification-dlq-test-group", "false", embeddedKafkaBroker);
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		Consumer<String, String> consumer = new DefaultKafkaConsumerFactory<>(
			props,
			new StringDeserializer(),
			new StringDeserializer()
		).createConsumer();

		try {
			embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, "greenroom.notification.dispatch.dlq");
			kafkaTemplate.send("greenroom.notification.dispatch", key, invalidMessage).join();

			var records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(10));
			assertThat(records.count()).isPositive();
			assertThat(records.iterator().next().value()).isEqualTo(invalidMessage);
		} finally {
			consumer.close();
		}
	}
}
