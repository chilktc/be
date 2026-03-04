package be.greenroom.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import be.greenroom.notification.dto.event.GreenroomDifficultyResolvedEvent;
import be.greenroom.notification.dto.event.GreenroomNotificationPreferenceUpdatedEvent;
import be.greenroom.notification.dto.event.GreenroomSessionCompletedEvent;
import be.greenroom.notification.dto.request.ResolveDifficultyRequest;
import be.greenroom.notification.dto.request.UpdateNotificationPreferenceRequest;
import be.greenroom.notification.producer.GreenroomNotificationEventProducer;

@ExtendWith(MockitoExtension.class)
@DisplayName("GreenroomNotificationCommandService 단위 테스트")
class GreenroomNotificationCommandServiceTest {

	@Mock
	private GreenroomNotificationEventProducer producer;

	@InjectMocks
	private GreenroomNotificationCommandService commandService;

	@Test
	@DisplayName("completeTicket은 session-completed 이벤트를 발행한다")
	void 티켓_완료시__세션완료_이벤트_발행() {
		// given
		UUID userId = UUID.randomUUID();
		UUID ticketId = UUID.randomUUID();
		UpdateNotificationPreferenceRequest request = new UpdateNotificationPreferenceRequest(17, 31, "Asia/Seoul");

		// when
		commandService.completeTicket(userId, ticketId, request);

		// then
		ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
		verify(producer).publishSessionCompleted(captor.capture());
		GreenroomSessionCompletedEvent event = (GreenroomSessionCompletedEvent) captor.getValue();

		assertThat(event.userId()).isEqualTo(userId);
		assertThat(event.ticketId()).isEqualTo(ticketId);
		assertThat(event.preferredHour()).isEqualTo(17);
		assertThat(event.preferredMinute()).isEqualTo(31);
		assertThat(event.timezone()).isEqualTo("Asia/Seoul");
	}

	@Test
	@DisplayName("updateNotificationPreference는 preference-updated 이벤트를 발행한다")
	void 선호시간_변경시__선호시간변경_이벤트_발행() {
		// given
		UUID userId = UUID.randomUUID();
		UUID ticketId = UUID.randomUUID();
		UpdateNotificationPreferenceRequest request = new UpdateNotificationPreferenceRequest(22, 15, "Asia/Seoul");

		// when
		commandService.updateNotificationPreference(userId, ticketId, request);

		// then
		ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
		verify(producer).publishPreferenceUpdated(captor.capture());
		GreenroomNotificationPreferenceUpdatedEvent event = (GreenroomNotificationPreferenceUpdatedEvent) captor.getValue();

		assertThat(event.ticketId()).isEqualTo(ticketId);
		assertThat(event.preferredHour()).isEqualTo(22);
		assertThat(event.preferredMinute()).isEqualTo(15);
		assertThat(event.timezone()).isEqualTo("Asia/Seoul");
	}

	@Test
	@DisplayName("resolveDifficulty는 difficulty-resolved 이벤트를 발행한다")
	void 어려움_해결시__해결완료_이벤트_발행() {
		// given
		UUID userId = UUID.randomUUID();
		UUID ticketId = UUID.randomUUID();

		// when
		commandService.resolveDifficulty(userId, ticketId, new ResolveDifficultyRequest("USER"));

		// then
		ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
		verify(producer).publishDifficultyResolved(captor.capture());
		GreenroomDifficultyResolvedEvent event = (GreenroomDifficultyResolvedEvent) captor.getValue();

		assertThat(event.ticketId()).isEqualTo(ticketId);
	}
}
