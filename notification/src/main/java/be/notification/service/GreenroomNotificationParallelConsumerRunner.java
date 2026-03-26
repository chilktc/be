package be.notification.service;

import static java.util.Collections.singleton;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import be.notification.event.GreenroomNotificationDispatchRequestEvent;
import io.confluent.parallelconsumer.ParallelConsumerOptions;
import io.confluent.parallelconsumer.ParallelStreamProcessor;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "notification.dispatch.parallel-consumer.enabled", havingValue = "true")
public class GreenroomNotificationParallelConsumerRunner {

	private static final String TOPIC = "greenroom.notification.dispatch";
	private static final String DLQ_TOPIC = "greenroom.notification.dispatch.dlq";
	private static final String GROUP_ID = "greenroom-notification-dispatch-group";

	private final KafkaProperties kafkaProperties;
	private final KafkaTemplate<String, String> kafkaTemplate;
	private final ObjectMapper objectMapper;
	private final GreenroomNotificationDispatchBatchService dispatchBatchService;
	private final GreenroomNotificationDispatchBatchBufferService dispatchBatchBufferService;

	public RunningConsumer start() {
		Map<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties());
		props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

		Consumer<String, String> consumer = new KafkaConsumer<>(props);
		var options = ParallelConsumerOptions.<String, String>builder()
			.consumer(consumer)
			.ordering(ParallelConsumerOptions.ProcessingOrder.UNORDERED)
			.maxConcurrency(10)
			.commitMode(ParallelConsumerOptions.CommitMode.PERIODIC_CONSUMER_ASYNCHRONOUS)
			.ignoreReflectiveAccessExceptionsForAutoCommitDisabledCheck(true)
			.build();
		ParallelStreamProcessor<String, String> processor = ParallelStreamProcessor.createEosStreamProcessor(options);
		processor.subscribe(singleton(TOPIC));

		ExecutorService pollExecutor = Executors.newSingleThreadExecutor(runnable -> {
			Thread thread = new Thread(runnable);
			thread.setName("greenroom-parallel-consumer-poller");
			return thread;
		});

		Future<?> pollFuture = pollExecutor.submit(() -> processor.poll(context -> {
			try {
				handleMessage(context.getSingleConsumerRecord().value());
			} catch (Exception exception) {
				publishToDlq(context.getSingleConsumerRecord().key(), context.getSingleConsumerRecord().value(), exception);
			}
		}));

		return new RunningConsumer(processor, pollExecutor, pollFuture, dispatchBatchBufferService);
	}

	private void handleMessage(String message) throws Exception {
		GreenroomNotificationDispatchRequestEvent event = objectMapper.readValue(message, GreenroomNotificationDispatchRequestEvent.class);
		dispatchBatchBufferService.accept(dispatchBatchService.toPersistenceCommand(event));
	}

	private void publishToDlq(String key, String message, Exception exception) {
		try {
			kafkaTemplate.send(DLQ_TOPIC, key, message).join();
		} catch (Exception dlqException) {
			throw new IllegalStateException("Parallel consumer DLQ publish failed", dlqException);
		}
	}

	public record RunningConsumer(
		ParallelStreamProcessor<String, String> processor,
		ExecutorService pollExecutor,
		Future<?> pollFuture,
		GreenroomNotificationDispatchBatchBufferService dispatchBatchBufferService
	) implements AutoCloseable {
		public void rethrowIfFailed() {
			if (!pollFuture.isDone()) {
				return;
			}
			try {
				pollFuture.get();
			} catch (Exception exception) {
				throw new IllegalStateException("Parallel consumer poll loop failed", exception);
			}
		}

		@Override
		public void close() {
			dispatchBatchBufferService.flushAll();
			processor.close();
			pollExecutor.shutdownNow();
		}
	}
}
