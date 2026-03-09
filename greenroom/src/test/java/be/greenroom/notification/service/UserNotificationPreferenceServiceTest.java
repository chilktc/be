package be.greenroom.notification.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import be.greenroom.notification.domain.UserNotificationPreference;
import be.greenroom.notification.event.GreenroomUserNotificationPreferenceUpdatedEvent;
import be.greenroom.notification.repository.UserNotificationPreferenceRepository;

@ExtendWith(MockitoExtension.class)
class UserNotificationPreferenceServiceTest {

	@Mock
	private UserNotificationPreferenceRepository preferenceRepository;
	@Mock
	private GreenroomNotificationEventPublisher eventPublisher;

	@InjectMocks
	private UserNotificationPreferenceService service;

	@Test
	@DisplayName("알림 설정 변경 시 USER_NOTIFICATION_PREFERENCE_UPDATED 이벤트를 발행한다")
	void 알림설정변경_이벤트발행() {
		UUID userId = UUID.randomUUID();
		when(preferenceRepository.findById(userId)).thenReturn(Optional.of(UserNotificationPreference.create(userId, true)));

		service.toggle(userId);

		verify(preferenceRepository).save(any(UserNotificationPreference.class));
		ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
		verify(eventPublisher).publish(org.mockito.ArgumentMatchers.eq(userId.toString()), captor.capture());
		org.assertj.core.api.Assertions.assertThat(captor.getValue())
			.isInstanceOf(GreenroomUserNotificationPreferenceUpdatedEvent.class);
		GreenroomUserNotificationPreferenceUpdatedEvent event =
			(GreenroomUserNotificationPreferenceUpdatedEvent) captor.getValue();
		org.assertj.core.api.Assertions.assertThat(event.enabled()).isFalse();
	}
}
