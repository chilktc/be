package be.notification.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "notification.dispatch.parallel-consumer.auto-startup", havingValue = "true")
public class GreenroomNotificationParallelConsumerLifecycle implements SmartLifecycle {

	private final GreenroomNotificationParallelConsumerRunner runner;

	private GreenroomNotificationParallelConsumerRunner.RunningConsumer runningConsumer;
	private volatile boolean running;

	@Override
	public void start() {
		if (running) {
			return;
		}
		this.runningConsumer = runner.start();
		this.running = true;
	}

	@Override
	public void stop() {
		if (!running) {
			return;
		}
		if (runningConsumer != null) {
			runningConsumer.close();
		}
		this.running = false;
	}

	@Override
	public boolean isAutoStartup() {
		return true;
	}

	@Override
	public boolean isRunning() {
		return running;
	}

	@Override
	public int getPhase() {
		return Integer.MAX_VALUE;
	}
}
