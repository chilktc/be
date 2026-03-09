package be.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import be.notification.domain.GreenroomNotificationHistory;
import be.notification.domain.GreenroomTemplateCode;
import be.notification.domain.SendResult;
import be.notification.repository.GreenroomNotificationHistoryRepository;
import be.notification.template.GreenroomTemplate;
import be.notification.template.GreenroomTemplateRegistry;

@ExtendWith(MockitoExtension.class)
class GreenroomNotificationDispatchServiceTest {

	@Mock
	private GreenroomTemplateRegistry templateRegistry;
	@Mock
	private GreenroomNotificationHistoryRepository historyRepository;

	@InjectMocks
	private GreenroomNotificationDispatchService dispatchService;

	@Test
	@DisplayName("정상 발송 시 성공 이력을 저장한다")
	void 정상발송_성공이력저장() {
		// given
		when(templateRegistry.get(any(GreenroomTemplateCode.class)))
			.thenReturn(new GreenroomTemplate("s", "b", "c"));
		when(historyRepository.existsByIdempotencyKey(any())).thenReturn(false);

		// when
		boolean result = dispatchService.sendEmail(UUID.randomUUID(), UUID.randomUUID(), 1);

		// then
		assertThat(result).isTrue();
		ArgumentCaptor<GreenroomNotificationHistory> captor = ArgumentCaptor.forClass(GreenroomNotificationHistory.class);
		verify(historyRepository).save(captor.capture());
		assertThat(captor.getValue().getResult()).isEqualTo(SendResult.SUCCESS);
	}

	@Test
	@DisplayName("성공 저장 실패가 반복되면 최대 3회 실패 이력을 남기고 false를 반환한다")
	void 실패재시도_3회() {
		// given
		when(templateRegistry.get(any(GreenroomTemplateCode.class)))
			.thenReturn(new GreenroomTemplate("s", "b", "c"));
		when(historyRepository.existsByIdempotencyKey(any())).thenReturn(false);
		when(historyRepository.save(any(GreenroomNotificationHistory.class))).thenAnswer(invocation -> {
			GreenroomNotificationHistory history = invocation.getArgument(0);
			if (history.getResult() == SendResult.SUCCESS) {
				throw new RuntimeException("forced");
			}
			return history;
		});

		// when
		boolean result = dispatchService.sendEmail(UUID.randomUUID(), UUID.randomUUID(), 1);

		// then
		assertThat(result).isFalse();
		verify(historyRepository, atLeast(6)).save(any(GreenroomNotificationHistory.class));
	}
}
