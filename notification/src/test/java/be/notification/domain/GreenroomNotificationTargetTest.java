package be.notification.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GreenroomNotificationTargetTest {

	private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

	@Test
	@DisplayName("nextSendAt은 D+1, D+3, D+7, D+21, 이후 14일 간격으로 계산된다")
	void 다음발송시각_계산_검증() {
		// given
		LocalDateTime createdAt = LocalDateTime.of(2026, 3, 1, 10, 0);
		GreenroomNotificationTarget target = GreenroomNotificationTarget.create(
			UUID.randomUUID(),
			UUID.randomUUID(),
			createdAt,
			true
		);

		// when then
		assertThat(target.getNextSequence()).isEqualTo(1);
		assertThat(target.getNextSendAt().atZone(SEOUL_ZONE).toLocalDateTime())
			.isEqualTo(LocalDateTime.of(2026, 3, 2, 8, 30));

		target.advanceAfterSuccess();
		assertThat(target.getNextSequence()).isEqualTo(2);
		assertThat(target.getNextSendAt().atZone(SEOUL_ZONE).toLocalDateTime())
			.isEqualTo(LocalDateTime.of(2026, 3, 4, 8, 30));

		target.advanceAfterSuccess();
		assertThat(target.getNextSequence()).isEqualTo(3);
		assertThat(target.getNextSendAt().atZone(SEOUL_ZONE).toLocalDateTime())
			.isEqualTo(LocalDateTime.of(2026, 3, 8, 8, 30));

		target.advanceAfterSuccess();
		assertThat(target.getNextSequence()).isEqualTo(4);
		assertThat(target.getNextSendAt().atZone(SEOUL_ZONE).toLocalDateTime())
			.isEqualTo(LocalDateTime.of(2026, 3, 22, 8, 30));

		target.advanceAfterSuccess();
		assertThat(target.getNextSequence()).isEqualTo(5);
		assertThat(target.getNextSendAt().atZone(SEOUL_ZONE).toLocalDateTime())
			.isEqualTo(LocalDateTime.of(2026, 4, 5, 8, 30));
	}
}
